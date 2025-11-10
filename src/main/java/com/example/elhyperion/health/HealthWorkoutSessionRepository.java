package com.example.elhyperion.health;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HealthWorkoutSessionRepository extends JpaRepository<HealthWorkoutSession, Long> {
    List<HealthWorkoutSession> findBySessionDateBetween(LocalDate from, LocalDate to);
    Page<HealthWorkoutSession> findBySessionDateBetween(LocalDate from, LocalDate to, Pageable pageable);
    Page<HealthWorkoutSession> findByRoutine_Id(Long routineId, Pageable pageable);
}
