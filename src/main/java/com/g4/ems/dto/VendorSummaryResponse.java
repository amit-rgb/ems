package com.g4.ems.dto;

public record VendorSummaryResponse(
        Long id,
        String vendorName,
        String contactNumber
) {
}
