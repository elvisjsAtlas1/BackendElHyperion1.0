package com.example.elhyperion.health;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthWorkoutRoutineRepository routines;
    private final HealthWorkoutSessionRepository sessions;

    public HealthController(HealthWorkoutRoutineRepository routines, HealthWorkoutSessionRepository sessions) {
        this.routines = routines; this.sessions = sessions;
    }

    // ====== RUTINAS ======
    public record RoutineReq(@NotBlank String name, String description) {}

    @GetMapping("/routines")
    public Page<HealthWorkoutRoutine> listRoutines(@PageableDefault(size=20, sort="id", direction=Sort.Direction.DESC) Pageable p){
        return routines.findAll(p);
    }

    @PostMapping("/routines")
    public HealthWorkoutRoutine createRoutine(@Valid @RequestBody RoutineReq req){
        return routines.save(HealthWorkoutRoutine.builder().name(req.name()).description(req.description()).build());
    }

    @PatchMapping("/routines/{id}")
    public ResponseEntity<HealthWorkoutRoutine> updateRoutine(@PathVariable Long id, @RequestBody RoutineReq req){
        return routines.findById(id).map(r -> {
            if (req.name()!=null && !req.name().isBlank()) r.setName(req.name());
            if (req.description()!=null) r.setDescription(req.description());
            return ResponseEntity.ok(routines.save(r));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/routines/{id}")
    public ResponseEntity<Void> deleteRoutine(@PathVariable Long id){
        if(!routines.existsById(id)) return ResponseEntity.notFound().build();
        routines.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ====== SESIONES ======
    public record SessionReq(Long routineId, String title, LocalDate sessionDate, Integer durationMinutes, Integer caloriesBurned, String notes){}

    @GetMapping("/sessions")
    public Page<HealthWorkoutSession> listSessions(@RequestParam(required=false) Long routineId,
                                                   @RequestParam(required=false) LocalDate from,
                                                   @RequestParam(required=false) LocalDate to,
                                                   @PageableDefault(size=20, sort="sessionDate", direction=Sort.Direction.DESC) Pageable p){
        if (routineId != null) return sessions.findByRoutine_Id(routineId, p);
        if (from != null && to != null) return sessions.findBySessionDateBetween(from, to, p);
        return sessions.findAll(p);
    }

    @PostMapping("/sessions")
    public ResponseEntity<HealthWorkoutSession> createSession(@RequestBody SessionReq req){
        if (req.sessionDate()==null) return ResponseEntity.badRequest().build();
        var s = new HealthWorkoutSession();
        if (req.routineId()!=null) routines.findById(req.routineId()).ifPresent(s::setRoutine);
        s.setTitle(req.title());
        s.setSessionDate(req.sessionDate());
        s.setDurationMinutes(req.durationMinutes()==null?0:req.durationMinutes());
        s.setCaloriesBurned(req.caloriesBurned()==null?0:req.caloriesBurned());
        s.setNotes(req.notes());
        return ResponseEntity.ok(sessions.save(s));
    }

    @PatchMapping("/sessions/{id}")
    public ResponseEntity<HealthWorkoutSession> updateSession(@PathVariable Long id, @RequestBody SessionReq req){
        return sessions.findById(id).map(s -> {
            if (req.routineId()!=null) routines.findById(req.routineId()).ifPresent(s::setRoutine);
            if (req.title()!=null) s.setTitle(req.title());
            if (req.sessionDate()!=null) s.setSessionDate(req.sessionDate());
            if (req.durationMinutes()!=null) s.setDurationMinutes(req.durationMinutes());
            if (req.caloriesBurned()!=null) s.setCaloriesBurned(req.caloriesBurned());
            if (req.notes()!=null) s.setNotes(req.notes());
            return ResponseEntity.ok(sessions.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id){
        if(!sessions.existsById(id)) return ResponseEntity.notFound().build();
        sessions.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ====== KPI simple: total sesiones/minutos/calorías en rango ======
    @GetMapping("/stats/workouts")
    public Map<String, Integer> workoutStats(@RequestParam LocalDate from, @RequestParam LocalDate to){
        var list = sessions.findBySessionDateBetween(from, to);
        int sessionsCount = list.size();
        int minutes = list.stream().map(s -> s.getDurationMinutes()==null?0:s.getDurationMinutes()).reduce(0, Integer::sum);
        int calories = list.stream().map(s -> s.getCaloriesBurned()==null?0:s.getCaloriesBurned()).reduce(0, Integer::sum);
        return Map.of("sessions", sessionsCount, "minutes", minutes, "calories", calories);
    }
}
