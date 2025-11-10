package com.example.elhyperion.grade;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByCourse_Id(Long courseId);
    Page<Grade> findByCourse_Id(Long courseId, Pageable pageable);
    List<Grade> findByCourse_IdAndGradedAtBetween(Long courseId, LocalDate from, LocalDate to);

    @Query("""
    select coalesce(sum(g.score),0), coalesce(sum(g.maxScore),0)
    from Grade g
    where g.course.id = :courseId
  """)
    List<Object[]> sumScoreAndMaxByCourse(Long courseId);

    @Query("""
    select coalesce(sum(g.score),0), coalesce(sum(g.maxScore),0)
    from Grade g
    where g.course.id = :courseId and g.gradedAt between :from and :to
  """)
    List<Object[]> sumScoreAndMaxByCourseInRange(Long courseId, LocalDate from, LocalDate to);

    @Query("""
    select coalesce(avg(g.score / g.maxScore * 100),0) 
    from Grade g where g.course.id = :courseId
  """)
    BigDecimal avgPercentByCourse(Long courseId);

    // opcional
    List<Grade> findByCourse_IdAndWeightPercentIsNotNull(Long courseId);

}
