package com.g4.ems.dto;

import com.g4.ems.domain.enums.MaterialType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ConsumeInventoryRequest(
        @NotNull Long projectId,
        @NotNull MaterialType materialType,
        @NotNull @DecimalMin(value = "0.000001", inclusive = true) BigDecimal quantityToConsume
) {
}
