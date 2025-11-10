package com.example.elhyperion.personal;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="personal_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PersonalTask {
    public enum Status { PENDING, DONE, MISSED }

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false, length=500) private String description;
    private LocalDate dueDate;   // opcional
    private LocalTime dueTime;   // opcional
    @Column(nullable=false) private boolean notify; // recordatorio sí/no
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.PENDING;

    private java.time.Instant createdAt; private java.time.Instant updatedAt;
    @PrePersist void pp(){ createdAt=java.time.Instant.now(); updatedAt=createdAt; }
    @PreUpdate  void pu(){ updatedAt=java.time.Instant.now(); }
}
