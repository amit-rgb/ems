package com.g4.ems.controller;

import com.g4.ems.domain.VendorLedger;
import com.g4.ems.dto.RegisterVendorRequest;
import com.g4.ems.dto.VendorSummaryResponse;
import com.g4.ems.exception.BusinessValidationException;
import com.g4.ems.repository.VendorLedgerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorLedgerRepository vendorLedgerRepository;

    @GetMapping
    public List<VendorSummaryResponse> getVendors() {
        return vendorLedgerRepository.findAllByOrderByVendorNameAsc()
                .stream()
                .map(v -> new VendorSummaryResponse(v.getId(), v.getVendorName(), v.getContactNumber()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendorSummaryResponse registerVendor(@Valid @RequestBody RegisterVendorRequest request) {
        vendorLedgerRepository.findByVendorNameIgnoreCase(request.vendorName().trim())
                .ifPresent(existing -> {
                    throw new BusinessValidationException("Vendor already exists: " + existing.getVendorName());
                });

        VendorLedger saved = vendorLedgerRepository.save(VendorLedger.builder()
                .vendorName(request.vendorName().trim())
                .contactNumber(request.contactNumber().trim())
                .totalAdvancePaid(BigDecimal.ZERO)
                .totalBillAmountSettled(BigDecimal.ZERO)
                .netOutstandingBalance(BigDecimal.ZERO)
                .build());

        return new VendorSummaryResponse(saved.getId(), saved.getVendorName(), saved.getContactNumber());
    }
}
