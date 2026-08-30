package com.g4.ems.repository;

import com.g4.ems.domain.VendorLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorLedgerRepository extends JpaRepository<VendorLedger, Long> {
    Optional<VendorLedger> findByVendorNameIgnoreCase(String vendorName);
    List<VendorLedger> findAllByOrderByVendorNameAsc();
}
