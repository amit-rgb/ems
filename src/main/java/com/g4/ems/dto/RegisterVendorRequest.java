package com.g4.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterVendorRequest(
        @NotBlank @Size(max = 120) String vendorName,
        @NotBlank @Pattern(regexp = "^[0-9+\\-\\s]{7,20}$") String contactNumber
) {
}
