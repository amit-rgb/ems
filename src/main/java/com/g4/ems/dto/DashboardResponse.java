package com.g4.ems.dto;

import com.g4.ems.domain.enums.MaterialType;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardResponse(
        BigDecimal totalSpent,
        BigDecimal remainingBudget,
        BigDecimal totalShiftingOrUnloadingCharges,
        Map<MaterialType, BigDecimal> currentStockLevels,
        Map<String, BigDecimal> netVendorBalancesByStage
) {
}
