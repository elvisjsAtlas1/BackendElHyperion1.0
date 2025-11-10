package com.example.elhyperion.health;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="health_workout_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HealthWorkoutSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="routine_id") private HealthWorkoutRoutine routine; // opcional
    private String title;
    @Column(nullable=false) private LocalDate sessionDate;
    private Integer durationMinutes;         // 0..n
    private Integer caloriesBurned;          // 0..n
    @Column(columnDefinition="TEXT") private String notes;
    private java.time.Instant createdAt; private java.time.Instant updatedAt;
    @PrePersist void pp(){ createdAt=java.time.Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=java.time.Instant.now(); }
}
