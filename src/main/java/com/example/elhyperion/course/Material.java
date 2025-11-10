package com.example.elhyperion.course;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Material {

    public enum Type { DOC, IMG, LINK, TEXT }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne @JoinColumn(name = "week_id")
    private CourseWeek week;   // puede ser null

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Type type;

    @Column(columnDefinition = "TEXT")
    private String infoText;   // contenido breve opcional

    @Column(columnDefinition = "TEXT")
    private String url;        // link/archivo

    private String mime;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void pp(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void pu(){ updatedAt = Instant.now(); }
}
