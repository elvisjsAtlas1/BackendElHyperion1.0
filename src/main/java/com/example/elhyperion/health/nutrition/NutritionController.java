package com.example.elhyperion.health.nutrition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/nutrition")
public class NutritionController {

    private final NutritionMealRepository meals;

    public NutritionController(NutritionMealRepository meals) { this.meals = meals; }

    // Listado
    @GetMapping("/meals")
    public Page<NutritionMeal> list(@RequestParam(required=false) LocalDate from,
                                    @RequestParam(required=false) LocalDate to,
                                    @RequestParam(required=false) NutritionMeal.MealType type,
                                    @PageableDefault(size=20, sort="mealDate", direction=Sort.Direction.DESC) Pageable p){
        if (from!=null && to!=null) return meals.findByMealDateBetween(from, to, p);
        if (type!=null) return meals.findByMealType(type, p);
        return meals.findAll(p);
    }

    public record UpsertReq(
            @NotNull LocalDate mealDate,
            @NotNull NutritionMeal.MealType mealType,
            Integer calories,
            BigDecimal proteinG, BigDecimal carbsG, BigDecimal fatG,
            String notes
    ){}

    @PostMapping("/meals")
    public NutritionMeal create(@Valid @RequestBody UpsertReq req){
        return meals.save(NutritionMeal.builder()
                .mealDate(req.mealDate())
                .mealType(req.mealType())
                .calories(req.calories())
                .proteinG(req.proteinG())
                .carbsG(req.carbsG())
                .fatG(req.fatG())
                .notes(req.notes())
                .build());
    }

    @PatchMapping("/meals/{id}")
    public ResponseEntity<NutritionMeal> update(@PathVariable Long id, @RequestBody UpsertReq req){
        return meals.findById(id).map(m -> {
            if (req.mealDate()!=null) m.setMealDate(req.mealDate());
            if (req.mealType()!=null) m.setMealType(req.mealType());
            if (req.calories()!=null) m.setCalories(req.calories());
            if (req.proteinG()!=null) m.setProteinG(req.proteinG());
            if (req.carbsG()!=null) m.setCarbsG(req.carbsG());
            if (req.fatG()!=null) m.setFatG(req.fatG());
            if (req.notes()!=null) m.setNotes(req.notes());
            return ResponseEntity.ok(meals.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(!meals.existsById(id)) return ResponseEntity.notFound().build();
        meals.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // KPI: totales y promedio diarios en un rango
    @GetMapping("/stats/macros")
    public Map<String, Object> macros(@RequestParam LocalDate from, @RequestParam LocalDate to){
        var list = meals.findByMealDateBetween(from, to);
        int days = Math.max(1, (int) (to.toEpochDay() - from.toEpochDay() + 1));

        int cal = list.stream().map(m -> m.getCalories()==null?0:m.getCalories()).reduce(0, Integer::sum);
        var p = list.stream().map(m -> m.getProteinG()==null?BigDecimal.ZERO:m.getProteinG())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var c = list.stream().map(m -> m.getCarbsG()==null?BigDecimal.ZERO:m.getCarbsG())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var f = list.stream().map(m -> m.getFatG()==null?BigDecimal.ZERO:m.getFatG())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalCalories", cal,
                "totalProteinG", p,
                "totalCarbsG", c,
                "totalFatG", f,
                "avgCaloriesPerDay", cal / days
        );
    }
}
