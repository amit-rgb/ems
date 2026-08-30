package com.g4.ems.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vendor_ledger")
@Check(constraints = "total_advance_paid >= 0 and total_bill_amount_settled >= 0")
public class VendorLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_name", nullable = false, unique = true, length = 120)
    private String vendorName;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Builder.Default
    @Column(name = "total_advance_paid", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAdvancePaid = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_bill_amount_settled", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalBillAmountSettled = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "net_outstanding_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal netOutstandingBalance = BigDecimal.ZERO;

    public void recomputeOutstandingBalance() {
        this.netOutstandingBalance = this.totalBillAmountSettled.subtract(this.totalAdvancePaid);
    }
}
