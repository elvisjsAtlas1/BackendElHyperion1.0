package com.example.elhyperion.note;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByCourse_Id(Long courseId, Pageable pageable);
    Page<Note> findByTitleContainingIgnoreCase(String q, Pageable pageable);
}
