package com.example.elhyperion.finance;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, Long> {

    Page<FinanceEntry> findByEntryDateBetween(LocalDate from, LocalDate to, Pageable pageable);
    Page<FinanceEntry> findByType(FinanceEntry.Type type, Pageable pageable);
    Page<FinanceEntry> findByTypeAndEntryDateBetween(FinanceEntry.Type type, LocalDate from, LocalDate to, Pageable pageable);

    List<FinanceEntry> findByEntryDateBetween(LocalDate from, LocalDate to);

    @Query("""
    select coalesce(sum(case when f.type = 'INCOME' then f.amount else 0 end), 0)
    from FinanceEntry f
    where f.entryDate between :from and :to
  """)
    BigDecimal sumIncome(LocalDate from, LocalDate to);

    @Query("""
    select coalesce(sum(case when f.type = 'EXPENSE' then f.amount else 0 end), 0)
    from FinanceEntry f
    where f.entryDate between :from and :to
  """)
    BigDecimal sumExpense(LocalDate from, LocalDate to);
}
