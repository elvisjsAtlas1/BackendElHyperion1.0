package com.example.elhyperion.finance;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "finance_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinanceEntry {

    public enum Type { INCOME, EXPENSE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String category;

    @Column(length = 500)
    private String note;

    private Instant createdAt;
    private Instant updatedAt;
    @PrePersist void pp(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = Instant.now(); }
}
