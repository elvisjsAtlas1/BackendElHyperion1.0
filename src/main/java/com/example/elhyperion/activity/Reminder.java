package com.example.elhyperion.activity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "reminders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reminder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(nullable = false)
    private Instant remindAt;
}
