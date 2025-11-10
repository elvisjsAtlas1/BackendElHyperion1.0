package com.example.elhyperion.activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByActivity_IdOrderByRemindAtAsc(Long activityId);

    // para limpiar/actualizar rápido
    long deleteByActivity_Id(Long activityId);

    // recordatorios antes de cierto instante (útil para cron interno)
    List<Reminder> findByRemindAtBefore(Instant when);
}
