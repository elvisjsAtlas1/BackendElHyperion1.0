package com.example.elhyperion.grade;

import com.example.elhyperion.activity.ActivityRepository;
import com.example.elhyperion.course.CourseRepository;
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
@RequestMapping("/grades")
public class GradeController {

    private final GradeRepository grades;
    private final CourseRepository courses;
    private final ActivityRepository activities;

    public GradeController(GradeRepository grades, CourseRepository courses, ActivityRepository activities) {
        this.grades = grades; this.courses = courses; this.activities = activities;
    }

    // Listado
    @GetMapping
    public Page<Grade> list(@RequestParam(required = false) Long courseId,
                            @PageableDefault(size=20, sort="gradedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (courseId != null) return grades.findByCourse_Id(courseId, pageable);
        return grades.findAll(pageable);
    }

    public record UpsertReq(
            @NotNull Long courseId,
            Long activityId,
            @NotNull BigDecimal score,
            @NotNull BigDecimal maxScore,
            @NotNull LocalDate gradedAt
    ) {}

    @PostMapping
    public ResponseEntity<Grade> create(@Valid @RequestBody UpsertReq req) {
        var course = courses.findById(req.courseId()).orElse(null);
        if (course == null) return ResponseEntity.badRequest().build();

        var g = Grade.builder()
                .course(course)
                .score(req.score())
                .maxScore(req.maxScore())
                .gradedAt(req.gradedAt())
                .build();

        if (req.activityId() != null) {
            activities.findById(req.activityId()).ifPresent(g::setActivity);
        }
        return ResponseEntity.ok(grades.save(g));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Grade> update(@PathVariable Long id, @RequestBody UpsertReq req) {
        return grades.findById(id).map(g -> {
            if (req.courseId() != null) courses.findById(req.courseId()).ifPresent(g::setCourse);
            if (req.activityId() != null) activities.findById(req.activityId()).ifPresent(g::setActivity);
            if (req.score() != null) g.setScore(req.score());
            if (req.maxScore() != null) g.setMaxScore(req.maxScore());
            if (req.gradedAt() != null) g.setGradedAt(req.gradedAt());
            return ResponseEntity.ok(grades.save(g));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!grades.existsById(id)) return ResponseEntity.notFound().build();
        grades.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ===== Resúmenes útiles =====

    @GetMapping("/course/{courseId}/summary")
    public Map<String, BigDecimal> summary(@PathVariable Long courseId) {
        var rows = grades.sumScoreAndMaxByCourse(courseId);
        BigDecimal gained = BigDecimal.ZERO, max = BigDecimal.ZERO;
        if (!rows.isEmpty()) {
            var r = rows.get(0);
            gained = (BigDecimal) r[0];
            max    = (BigDecimal) r[1];
        }
        var percent = max.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gained.multiply(BigDecimal.valueOf(100)).divide(max, 2, BigDecimal.ROUND_HALF_UP);
        return Map.of("score", gained, "maxScore", max, "percent", percent);
    }

    // ¿Qué necesito sacar en el próximo examen para llegar a X%?
    @GetMapping("/course/{courseId}/needed")
    public Map<String, BigDecimal> neededForTarget(@PathVariable Long courseId,
                                                   @RequestParam BigDecimal targetPercent,
                                                   @RequestParam BigDecimal nextMaxScore) {
        var rows = grades.sumScoreAndMaxByCourse(courseId);
        BigDecimal gained = BigDecimal.ZERO, max = BigDecimal.ZERO;
        if (!rows.isEmpty()) {
            var r = rows.get(0);
            gained = (BigDecimal) r[0];
            max    = (BigDecimal) r[1];
        }
        // (gained + x) / (max + nextMax) = target%
        var target = targetPercent.divide(BigDecimal.valueOf(100));
        var needed = target.multiply(max.add(nextMaxScore)).subtract(gained);
        if (needed.compareTo(BigDecimal.ZERO) < 0) needed = BigDecimal.ZERO;
        if (needed.compareTo(nextMaxScore) > 0) needed = nextMaxScore;
        return Map.of("neededScore", needed);
    }

    // ====== RESUMEN PONDERADO (persistente, usando data guardada) ======
    @GetMapping("/course/{courseId}/weighted-summary")
    public Map<String, Object> weightedSummary(@PathVariable Long courseId) {
        var all = grades.findByCourse_Id(courseId);
        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (var g : all) {
            if (g.getWeightPercent() == null) continue; // no cuenta para ponderado
            if (g.getMaxScore() == null || g.getMaxScore().compareTo(BigDecimal.ZERO) == 0) continue;

            // porcentaje obtenido en esa evaluación (0..100)
            BigDecimal percent = g.getScore()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(g.getMaxScore(), 4, java.math.RoundingMode.HALF_UP);

            // aporte ponderado = (percent * weight%) / 100
            BigDecimal contrib = percent.multiply(g.getWeightPercent())
                    .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

            weightedSum = weightedSum.add(contrib);
            totalWeight = totalWeight.add(g.getWeightPercent());
        }

        // Si el sílabo suma 100% → weightedSum ya es tu promedio ponderado final.
        // Si suma <100 (faltan evaluaciones), devolvemos también cuánto falta.
        BigDecimal missing = BigDecimal.valueOf(100).subtract(totalWeight);
        if (missing.compareTo(BigDecimal.ZERO) < 0) missing = BigDecimal.ZERO;

        return Map.of(
                "weightedPercent", weightedSum.setScale(2, java.math.RoundingMode.HALF_UP), // 0..100
                "usedWeight", totalWeight,
                "missingWeight", missing
        );
    }

    // ====== CALCULADORA DINÁMICA (no persiste) ======
    public record WeightedItem(BigDecimal score, BigDecimal maxScore, BigDecimal weightPercent) {}
    public record WeightedCalcReq(java.util.List<WeightedItem> items) {}
    public record WeightedCalcRes(BigDecimal weightedPercent, BigDecimal usedWeight) {}

    @PostMapping("/calc/weighted")
    public ResponseEntity<WeightedCalcRes> calcWeighted(@RequestBody WeightedCalcReq req) {
        if (req.items() == null || req.items().isEmpty())
            return ResponseEntity.badRequest().build();

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (var it : req.items()) {
            if (it == null || it.maxScore() == null || it.maxScore().compareTo(BigDecimal.ZERO) == 0) continue;
            if (it.weightPercent() == null || it.weightPercent().compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal percent = it.score()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(it.maxScore(), 4, java.math.RoundingMode.HALF_UP);

            BigDecimal contrib = percent.multiply(it.weightPercent())
                    .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);

            weightedSum = weightedSum.add(contrib);
            totalWeight = totalWeight.add(it.weightPercent());
        }

        return ResponseEntity.ok(new WeightedCalcRes(
                weightedSum.setScale(2, java.math.RoundingMode.HALF_UP),
                totalWeight.setScale(2, java.math.RoundingMode.HALF_UP)
        ));
    }

    // ====== ¿Cuánto necesito en la próxima evaluación para llegar a X% final? (dinámico) ======
    public record NeededReq(
            BigDecimal targetFinalPercent,   // ej. 60, 70
            BigDecimal remainingWeightPercent, // ej. peso de la próxima prueba (o suma de las que faltan)
            java.util.List<WeightedItem> current // items ya rendidos (score/max/weight)
    ) {}
    public record NeededRes(BigDecimal neededPercentOnNext) {}

    @PostMapping("/calc/needed")
    public ResponseEntity<NeededRes> needed(@RequestBody NeededReq req) {
        if (req.targetFinalPercent() == null || req.remainingWeightPercent() == null) {
            return ResponseEntity.badRequest().build();
        }

        // 1) promedio ponderado actual con los items rendidos
        var calc = calcWeighted(new WeightedCalcReq(req.current()));
        var body = calc.getBody();
        BigDecimal currentWeighted = (body == null ? BigDecimal.ZERO : body.weightedPercent());
        BigDecimal usedWeight = (body == null ? BigDecimal.ZERO : body.usedWeight());

        BigDecimal remainingWeight = req.remainingWeightPercent();
        BigDecimal totalAfter = usedWeight.add(remainingWeight);
        if (totalAfter.compareTo(BigDecimal.valueOf(100)) > 0) {
            // permitido, pero avisamos: se pasaría de 100
        }

        // Fórmula:
        // currentWeighted + x * (remainingWeight/100) = targetFinalPercent
        // => x = (target - currentWeighted) / (remainingWeight/100)
        if (remainingWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.ok(new NeededRes(BigDecimal.ZERO));
        }
        BigDecimal divisor = remainingWeight.divide(BigDecimal.valueOf(100), 8, java.math.RoundingMode.HALF_UP);
        BigDecimal needed = req.targetFinalPercent()
                .subtract(currentWeighted)
                .divide(divisor, 4, java.math.RoundingMode.HALF_UP);

        // clamp 0..100
        if (needed.compareTo(BigDecimal.ZERO) < 0) needed = BigDecimal.ZERO;
        if (needed.compareTo(BigDecimal.valueOf(100)) > 0) needed = BigDecimal.valueOf(100);

        return ResponseEntity.ok(new NeededRes(needed.setScale(2, java.math.RoundingMode.HALF_UP)));
    }
}
