package com.g4.ems.repository;

import com.g4.ems.domain.ConstructionExpense;
import com.g4.ems.domain.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ConstructionExpenseRepository extends JpaRepository<ConstructionExpense, Long> {

    @Query("""
            select coalesce(sum(e.amount), 0)
            from ConstructionExpense e
            where e.project.id = :projectId and e.status = :status
            """)
    BigDecimal sumByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ExpenseStatus status);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from ConstructionExpense e
            where e.project.id = :projectId
              and e.status = :status
              and e.isShiftingOrUnloadingCharge = true
            """)
    BigDecimal sumShiftingOrUnloadingByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") ExpenseStatus status);

    List<ConstructionExpense> findByProjectId(Long projectId);
}
