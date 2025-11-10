package com.example.elhyperion.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Listas simples
    List<Material> findByCourse_Id(Long courseId);
    List<Material> findByWeek_Id(Long weekId);

    // Sync delta
    List<Material> findByUpdatedAtAfter(Instant updatedAfter);

    // Combinaciones útiles (con paginación)
    Page<Material> findByCourse_IdAndWeek_Id(Long courseId, Long weekId, Pageable pageable);
    Page<Material> findByCourse_IdAndUpdatedAtAfter(Long courseId, Instant updatedAfter, Pageable pageable);
}
