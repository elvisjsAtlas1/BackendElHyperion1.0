package com.example.elhyperion.schedule;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findByCourse_Id(Long courseId);
    List<ScheduleItem> findByDayOfWeekOrderByStartTimeAsc(Integer dayOfWeek);
    Page<ScheduleItem> findByDayOfWeek(Integer dayOfWeek, Pageable pageable);
}
