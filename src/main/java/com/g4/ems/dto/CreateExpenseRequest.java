package com.g4.ems.dto;

import com.g4.ems.domain.enums.ConstructionStage;
import com.g4.ems.domain.enums.ExpenseCategory;
import com.g4.ems.domain.enums.ExpenseStatus;
import com.g4.ems.domain.enums.MaterialType;
import com.g4.ems.domain.enums.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExpenseRequest(
        @NotNull Long projectId,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
        @NotNull ExpenseCategory category,
        @NotNull ConstructionStage stage,
        @NotNull PaymentMode paymentMode,
        @Size(max = 120) String vendorName,
        @Size(max = 500) String description,
        @Size(max = 500) String billReceiptUrl,
        @NotNull ExpenseStatus status,
        boolean shiftingOrUnloadingCharge,
        @NotNull LocalDate expenseDate,
        MaterialType materialType,
        @DecimalMin(value = "0.000001", inclusive = true) BigDecimal receivedQuantity
) {
}
