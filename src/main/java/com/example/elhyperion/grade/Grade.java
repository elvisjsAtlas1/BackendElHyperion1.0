// grade/Grade.java
package com.example.elhyperion.grade;

import com.example.elhyperion.activity.Activity;
import com.example.elhyperion.course.Course;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "grades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Grade {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne @JoinColumn(name = "activity_id")
    private Activity activity; // opcional

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "weight_percent", precision = 5, scale = 2) // 0..100, NULL si no pondera
    private BigDecimal weightPercent;

    @Column(nullable = false)
    private LocalDate gradedAt;

    private Instant createdAt; private Instant updatedAt;
    @PrePersist void pp(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = Instant.now(); }
}
