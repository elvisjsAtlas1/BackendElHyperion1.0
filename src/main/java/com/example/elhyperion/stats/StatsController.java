package com.example.elhyperion.stats;

import com.example.elhyperion.activity.*;
import com.example.elhyperion.finance.FinanceEntryRepository;
import com.example.elhyperion.grade.Grade;
import com.example.elhyperion.grade.GradeRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final ActivityRepository activities;
    private final FinanceEntryRepository finance;
    private final GradeRepository grades;

    public StatsController(ActivityRepository activities, FinanceEntryRepository finance, GradeRepository grades) {
        this.activities = activities;
        this.finance = finance;
        this.grades = grades;
    }

    // ===== Actividades: conteo por estado + vencidas en rango =====
    @GetMapping("/activities")
    public Map<String, Object> activitiesStats(@RequestParam(required=false) LocalDate from,
                                               @RequestParam(required=false) LocalDate to) {
        var list = activities.findAll();
        long pending = list.stream().filter(a -> a.getStatus()== Activity.Status.PENDING).count();
        long done    = list.stream().filter(a -> a.getStatus()== Activity.Status.DONE).count();
        long missed  = list.stream().filter(a -> a.getStatus()== Activity.Status.MISSED).count();

        long overdue = list.stream()
                .filter(a -> a.getStatus()== Activity.Status.PENDING && a.getDueDate()!=null)
                .filter(a -> a.getDueDate().isBefore(LocalDate.now()))
                .count();

        // rango opcional
        if (from!=null && to!=null) {
            list = list.stream()
                    .filter(a -> a.getDueDate()!=null && !a.getDueDate().isBefore(from) && !a.getDueDate().isAfter(to))
                    .toList();
        }
        var byCourse = list.stream().collect(Collectors.groupingBy(a -> a.getCourse().getName(), Collectors.counting()));

        return Map.of(
                "pending", pending, "done", done, "missed", missed, "overdue", overdue,
                "byCourse", byCourse
        );
    }

    // ===== Finanzas: resumen simple en rango =====
    @GetMapping("/finance")
    public Map<String, Object> financeStats(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        var income  = finance.sumIncome(from, to);
        var expense = finance.sumExpense(from, to);
        var balance = income.subtract(expense);
        return Map.of("income", income, "expense", expense, "balance", balance);
    }

    // ===== Calificaciones: promedio por curso (porcentaje) =====
    @GetMapping("/grades/course-averages")
    public Map<Long, BigDecimal> gradeAverages() {
        var all = grades.findAll();
        Map<Long, List<Grade>> byCourse = all.stream().collect(Collectors.groupingBy(g -> g.getCourse().getId()));
        Map<Long, BigDecimal> result = new HashMap<>();
        for (var e : byCourse.entrySet()) {
            var list = e.getValue();
            BigDecimal gained = list.stream().map(Grade::getScore).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal max = list.stream().map(Grade::getMaxScore).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal percent = (max.compareTo(BigDecimal.ZERO)==0)
                    ? BigDecimal.ZERO
                    : gained.multiply(BigDecimal.valueOf(100)).divide(max, 2, java.math.RoundingMode.HALF_UP);
            result.put(e.getKey(), percent);
        }
        return result;
    }
}
