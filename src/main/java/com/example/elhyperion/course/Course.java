package com.example.elhyperion.course;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void pp(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = Instant.now(); }
}
