package com.g4.ems.domain;

import com.g4.ems.domain.enums.MaterialType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "material_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_project_material", columnNames = {"project_id", "material_type"})
)
@Check(constraints = "total_quantity_received >= 0 and total_quantity_consumed >= 0 and remaining_stock >= 0")
public class MaterialInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_inventory_project"))
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 40)
    private MaterialType materialType;

    @Builder.Default
    @Column(name = "total_quantity_received", nullable = false, precision = 19, scale = 6)
    private BigDecimal totalQuantityReceived = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_quantity_consumed", nullable = false, precision = 19, scale = 6)
    private BigDecimal totalQuantityConsumed = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "remaining_stock", nullable = false, precision = 19, scale = 6)
    private BigDecimal remainingStock = BigDecimal.ZERO;

    @Column(name = "last_updated_at", nullable = false)
    private OffsetDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.lastUpdatedAt = OffsetDateTime.now();
    }
}
