package com.example.elhyperion.health;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="health_workout_routines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HealthWorkoutRoutine {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(columnDefinition="TEXT") private String description;
    private Instant createdAt; private Instant updatedAt;
    @PrePersist void pp(){ createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=Instant.now(); }
}
