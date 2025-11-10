package com.example.elhyperion.finance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/finance/stats")
@RequiredArgsConstructor
public class FinanceStatsController {

    private final JdbcTemplate jdbc;

    public record Row(String period, BigDecimal income, BigDecimal expense, BigDecimal balance) {}

    @GetMapping("/daily")
    public List<Row> daily(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return jdbc.query("""
      SELECT DATE_FORMAT(entry_date,'%Y-%m-%d') period,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE 0 END) income,
             SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) expense,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE -amount END) balance
      FROM finance_entries
      WHERE entry_date BETWEEN ? AND ?
      GROUP BY entry_date
      ORDER BY entry_date
      """,
                (rs,i) -> new Row(
                        rs.getString("period"),
                        rs.getBigDecimal("income"),
                        rs.getBigDecimal("expense"),
                        rs.getBigDecimal("balance")
                ), from, to);
    }

    @GetMapping("/weekly")
    public List<Row> weekly(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return jdbc.query("""
      SELECT YEARWEEK(entry_date, 3) period,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE 0 END) income,
             SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) expense,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE -amount END) balance
      FROM finance_entries
      WHERE entry_date BETWEEN ? AND ?
      GROUP BY YEARWEEK(entry_date, 3)
      ORDER BY MIN(entry_date)
      """,
                (rs,i) -> new Row(
                        rs.getString("period"),
                        rs.getBigDecimal("income"),
                        rs.getBigDecimal("expense"),
                        rs.getBigDecimal("balance")
                ), from, to);
    }

    @GetMapping("/monthly")
    public List<Row> monthly(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        return jdbc.query("""
      SELECT DATE_FORMAT(entry_date,'%Y-%m') period,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE 0 END) income,
             SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) expense,
             SUM(CASE WHEN type='INCOME'  THEN amount ELSE -amount END) balance
      FROM finance_entries
      WHERE entry_date BETWEEN ? AND ?
      GROUP BY DATE_FORMAT(entry_date,'%Y-%m')
      ORDER BY period
      """,
                (rs,i) -> new Row(
                        rs.getString("period"),
                        rs.getBigDecimal("income"),
                        rs.getBigDecimal("expense"),
                        rs.getBigDecimal("balance")
                ), from, to);
    }
}
