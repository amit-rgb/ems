package com.g4.ems.service;

import com.g4.ems.domain.ConstructionExpense;
import com.g4.ems.domain.MaterialInventory;
import com.g4.ems.domain.Project;
import com.g4.ems.domain.VendorLedger;
import com.g4.ems.domain.enums.ConstructionStage;
import com.g4.ems.domain.enums.ExpenseCategory;
import com.g4.ems.domain.enums.ExpenseStatus;
import com.g4.ems.domain.enums.MaterialType;
import com.g4.ems.dto.ConsumeInventoryRequest;
import com.g4.ems.dto.CreateExpenseRequest;
import com.g4.ems.dto.DashboardResponse;
import com.g4.ems.dto.ExpenseResponse;
import com.g4.ems.exception.BusinessValidationException;
import com.g4.ems.exception.ResourceNotFoundException;
import com.g4.ems.repository.ConstructionExpenseRepository;
import com.g4.ems.repository.MaterialInventoryRepository;
import com.g4.ems.repository.ProjectRepository;
import com.g4.ems.repository.VendorLedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConstructionFinanceService {

    private final ProjectRepository projectRepository;
    private final VendorLedgerRepository vendorLedgerRepository;
    private final MaterialInventoryRepository materialInventoryRepository;
    private final ConstructionExpenseRepository constructionExpenseRepository;

    @Transactional
    public ExpenseResponse logExpenseAndUpdateBalances(CreateExpenseRequest request) {
        validateAmount(request.amount(), "Expense amount must be greater than zero.");

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + request.projectId()));

        VendorLedger vendor = resolveVendor(request.vendorName());
        if (request.category() == ExpenseCategory.CONTRACTOR_ADVANCE && vendor == null) {
            throw new BusinessValidationException("Vendor name is required when category is CONTRACTOR_ADVANCE.");
        }

        ConstructionExpense expense = ConstructionExpense.builder()
                .project(project)
                .amount(request.amount())
                .category(request.category())
                .stage(request.stage())
                .paymentMode(request.paymentMode())
                .vendor(vendor)
                .description(request.description())
                .billReceiptUrl(request.billReceiptUrl())
                .status(request.status())
                .isShiftingOrUnloadingCharge(request.shiftingOrUnloadingCharge())
                .expenseDate(request.expenseDate())
                .build();
        constructionExpenseRepository.save(expense);

        if (request.category() == ExpenseCategory.CONTRACTOR_ADVANCE) {
            vendor.setTotalAdvancePaid(vendor.getTotalAdvancePaid().add(request.amount()));
            vendor.recomputeOutstandingBalance();
            vendorLedgerRepository.save(vendor);
        } else if (vendor != null && request.status() == ExpenseStatus.PAID) {
            vendor.setTotalBillAmountSettled(vendor.getTotalBillAmountSettled().add(request.amount()));
            vendor.recomputeOutstandingBalance();
            vendorLedgerRepository.save(vendor);
        }

        updateInventoryForMaterialArrival(project, request);

        return new ExpenseResponse(expense.getId(), "Expense logged successfully.");
    }

    @Transactional
    public void consumeInventory(ConsumeInventoryRequest request) {
        validateAmount(request.quantityToConsume(), "Consumed quantity must be greater than zero.");

        MaterialInventory inventory = materialInventoryRepository
                .findByProjectIdAndMaterialType(request.projectId(), request.materialType())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for projectId=" + request.projectId() + ", materialType=" + request.materialType()
                ));

        BigDecimal updatedConsumed = inventory.getTotalQuantityConsumed().add(request.quantityToConsume());
        BigDecimal updatedRemaining = inventory.getTotalQuantityReceived().subtract(updatedConsumed);
        if (updatedRemaining.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Remaining stock cannot go negative.");
        }

        inventory.setTotalQuantityConsumed(updatedConsumed);
        inventory.setRemainingStock(updatedRemaining);
        materialInventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getProjectDashboard(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        BigDecimal totalSpent = constructionExpenseRepository.sumByProjectIdAndStatus(projectId, ExpenseStatus.PAID);
        BigDecimal remainingBudget = project.getTotalBudget().subtract(totalSpent);
        BigDecimal shiftingCharges = constructionExpenseRepository
                .sumShiftingOrUnloadingByProjectIdAndStatus(projectId, ExpenseStatus.PAID);

        Map<MaterialType, BigDecimal> stockLevels = new EnumMap<>(MaterialType.class);
        materialInventoryRepository.findByProjectId(projectId)
                .forEach(inv -> stockLevels.put(inv.getMaterialType(), inv.getRemainingStock()));

        Map<String, BigDecimal> netVendorBalancesByStage = stageWiseVendorNetBalance(projectId);

        return new DashboardResponse(
                totalSpent,
                remainingBudget,
                shiftingCharges,
                stockLevels,
                netVendorBalancesByStage
        );
    }

    private Map<String, BigDecimal> stageWiseVendorNetBalance(Long projectId) {
        Map<ConstructionStage, BigDecimal> stageBalances = new EnumMap<>(ConstructionStage.class);
        List<ConstructionExpense> expenses = constructionExpenseRepository.findByProjectId(projectId);
        for (ConstructionExpense expense : expenses) {
            if (Objects.isNull(expense.getVendor())) {
                continue;
            }
            BigDecimal delta = expense.getCategory() == ExpenseCategory.CONTRACTOR_ADVANCE
                    ? expense.getAmount().negate()
                    : expense.getAmount();
            stageBalances.merge(expense.getStage(), delta, BigDecimal::add);
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Map.Entry<ConstructionStage, BigDecimal> entry : stageBalances.entrySet()) {
            result.put(entry.getKey().name(), entry.getValue());
        }
        return result;
    }

    private void updateInventoryForMaterialArrival(Project project, CreateExpenseRequest request) {
        MaterialType materialType = resolveMaterialType(request.category(), request.materialType());
        if (materialType == null || request.receivedQuantity() == null) {
            return;
        }

        validateAmount(request.receivedQuantity(), "Received quantity must be greater than zero.");

        MaterialInventory inventory = materialInventoryRepository
                .findByProjectIdAndMaterialType(project.getId(), materialType)
                .orElseGet(() -> MaterialInventory.builder()
                        .project(project)
                        .materialType(materialType)
                        .build());

        inventory.setTotalQuantityReceived(inventory.getTotalQuantityReceived().add(request.receivedQuantity()));
        inventory.setRemainingStock(inventory.getTotalQuantityReceived().subtract(inventory.getTotalQuantityConsumed()));
        if (inventory.getRemainingStock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Remaining stock cannot go negative.");
        }
        materialInventoryRepository.save(inventory);
    }

    private VendorLedger resolveVendor(String vendorName) {
        if (vendorName == null || vendorName.isBlank()) {
            return null;
        }
        return vendorLedgerRepository.findByVendorNameIgnoreCase(vendorName.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with name: " + vendorName));
    }

    private MaterialType resolveMaterialType(ExpenseCategory category, MaterialType explicitMaterialType) {
        if (explicitMaterialType != null) {
            return explicitMaterialType;
        }
        return switch (category) {
            case CEMENT -> MaterialType.CEMENT_BAGS;
            case STEEL -> MaterialType.STEEL_TONS;
            case SAND -> MaterialType.SAND_BRASS;
            case BRICKS -> MaterialType.BRICKS_COUNT;
            case AGGREGATE -> MaterialType.AGGREGATE_BRASS;
            default -> null;
        };
    }

    private void validateAmount(BigDecimal amount, String message) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException(message);
        }
    }
}
