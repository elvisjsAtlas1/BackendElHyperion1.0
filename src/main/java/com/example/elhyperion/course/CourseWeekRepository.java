package com.example.elhyperion.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseWeekRepository extends JpaRepository<CourseWeek, Long> {

    List<CourseWeek> findByCourse_IdOrderByNumberAsc(Long courseId);
}
