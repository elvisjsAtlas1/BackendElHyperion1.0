package com.example.elhyperion.schedule;

import com.example.elhyperion.course.Course;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="schedule_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleItem {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="course_id")
    private Course course;

    @Column(nullable=false) private Integer dayOfWeek; // 1=Lunes .. 7=Domingo
    @Column(nullable=false) private LocalTime startTime;
    @Column(nullable=false) private LocalTime endTime;

    private String room;
    private String color;

    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;
    @PrePersist void pp(){ createdAt=java.time.Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=java.time.Instant.now(); }
}
