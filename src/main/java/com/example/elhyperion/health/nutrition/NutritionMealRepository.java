package com.example.elhyperion.health.nutrition;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NutritionMealRepository extends JpaRepository<NutritionMeal, Long> {
    List<NutritionMeal> findByMealDateBetween(LocalDate from, LocalDate to);
    Page<NutritionMeal> findByMealDateBetween(LocalDate from, LocalDate to, Pageable pageable);
    Page<NutritionMeal> findByMealType(NutritionMeal.MealType type, Pageable pageable);
}
