package com.example.elhyperion.health.nutrition;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name="nutrition_meals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NutritionMeal {

    public enum MealType { BREAKFAST, LUNCH, DINNER, SNACK }

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false) private LocalDate mealDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private MealType mealType;

    private Integer calories;
    @Column(precision=6, scale=2) private BigDecimal proteinG;
    @Column(precision=6, scale=2) private BigDecimal carbsG;
    @Column(precision=6, scale=2) private BigDecimal fatG;

    @Column(length=500) private String notes;

    private java.time.Instant createdAt; private java.time.Instant updatedAt;
    @PrePersist void pp(){ createdAt=java.time.Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=java.time.Instant.now(); }
}
