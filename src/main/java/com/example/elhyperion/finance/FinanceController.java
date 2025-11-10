package com.example.elhyperion.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/finance/entries")
public class FinanceController {

    private final FinanceEntryRepository repo;

    public FinanceController(FinanceEntryRepository repo) { this.repo = repo; }

    // Listado con filtros y paginación
    @GetMapping
    public Page<FinanceEntry> list(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) FinanceEntry.Type type,
            @PageableDefault(size = 20, sort = "entryDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (from != null && to != null && type != null) {
            return repo.findByTypeAndEntryDateBetween(type, from, to, pageable);
        } else if (from != null && to != null) {
            return repo.findByEntryDateBetween(from, to, pageable);
        } else if (type != null) {
            return repo.findByType(type, pageable);
        }
        return repo.findAll(pageable);
    }

    public record UpsertReq(
            @NotNull LocalDate entryDate,
            @NotNull FinanceEntry.Type type,
            @NotNull BigDecimal amount,
            String category,
            String note
    ) {}

    @PostMapping
    public FinanceEntry create(@Valid @RequestBody UpsertReq req) {
        return repo.save(FinanceEntry.builder()
                .entryDate(req.entryDate())
                .type(req.type())
                .amount(req.amount())
                .category(req.category())
                .note(req.note())
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FinanceEntry> update(@PathVariable Long id, @RequestBody UpsertReq req) {
        return repo.findById(id).map(e -> {
            if (req.entryDate() != null) e.setEntryDate(req.entryDate());
            if (req.type() != null) e.setType(req.type());
            if (req.amount() != null) e.setAmount(req.amount());
            if (req.category() != null) e.setCategory(req.category());
            if (req.note() != null) e.setNote(req.note());
            return ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Resumen rápido (ingresos/egresos/balance) para un rango
    public record Summary(BigDecimal income, BigDecimal expense, BigDecimal balance) {}

    @GetMapping("/summary")
    public Summary summary(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        var inc = repo.sumIncome(from, to);
        var exp = repo.sumExpense(from, to);
        return new Summary(inc, exp, inc.subtract(exp));
    }
}
