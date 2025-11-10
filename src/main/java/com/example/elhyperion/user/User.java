package com.example.elhyperion.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Column(nullable=false)
    private boolean enabled = true;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }
    @PreUpdate void preUpdate() {
        updatedAt = Instant.now();
    }
}
