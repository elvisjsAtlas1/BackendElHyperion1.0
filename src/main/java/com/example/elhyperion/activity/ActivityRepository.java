package com.example.elhyperion.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Filtros básicos
    List<Activity> findByCourse_Id(Long courseId);
    List<Activity> findByStatus(Activity.Status status);

    // Rango de fechas (solo por fecha)
    List<Activity> findByDueDateBetween(LocalDate from, LocalDate to);

    // Sync delta
    List<Activity> findByUpdatedAtAfter(Instant updatedAfter);

    // Combinados + paginación (para listas grandes)
    Page<Activity> findByCourse_IdAndStatus(Long courseId, Activity.Status status, Pageable pageable);
    Page<Activity> findByCourse_IdAndDueDateBetween(Long courseId, LocalDate from, LocalDate to, Pageable pageable);

    // “Próximas” pendientes (top N)
    List<Activity> findTop10ByStatusOrderByDueDateAsc(Activity.Status status);
}
