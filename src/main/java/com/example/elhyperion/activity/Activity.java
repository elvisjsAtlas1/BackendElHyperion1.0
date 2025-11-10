package com.example.elhyperion.activity;

import com.example.elhyperion.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity
@Table(name = "activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Activity {

    public enum Status { PENDING, DONE, MISSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private LocalDate dueDate;         // puede ser null
    private LocalTime dueTime;         // puede ser null

    @Column(nullable = false)
    private boolean notify = false;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status = Status.PENDING;

    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;

    @PrePersist void pp(){ createdAt = java.time.Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = java.time.Instant.now(); }
}
