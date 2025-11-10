package com.example.elhyperion.personal;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PersonalTaskRepository extends JpaRepository<PersonalTask, Long> {
    Page<PersonalTask> findByStatus(PersonalTask.Status status, Pageable pageable);
    Page<PersonalTask> findByDueDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    List<PersonalTask> findTop20ByStatusOrderByDueDateAsc(PersonalTask.Status status);
}
