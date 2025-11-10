package com.example.elhyperion.course;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "course_weeks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseWeek {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private Integer number;

    private String title;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void pp(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = Instant.now(); }
}
