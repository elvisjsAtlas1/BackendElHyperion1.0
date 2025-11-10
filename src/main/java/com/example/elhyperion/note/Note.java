package com.example.elhyperion.note;

import com.example.elhyperion.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name="notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Note {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name="course_id") private Course course;  // opcional
    @Column(nullable=false) private String title;
    @Column(columnDefinition="TEXT") private String content;
    private Instant createdAt; private Instant updatedAt;
    @PrePersist void pp(){ createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=Instant.now(); }
}
