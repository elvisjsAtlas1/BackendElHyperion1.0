package com.example.elhyperion.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Course> findByNameContainingIgnoreCase(String q, Pageable pageable);
}
