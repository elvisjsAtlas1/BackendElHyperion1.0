package com.example.elhyperion.schedule;

import com.example.elhyperion.course.CourseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleItemRepository repo;
    private final CourseRepository courses;

    public ScheduleController(ScheduleItemRepository repo, CourseRepository courses) {
        this.repo = repo; this.courses = courses;
    }

    // ===== Listas =====
    @GetMapping
    public Page<ScheduleItem> list(@RequestParam(required=false) Integer dayOfWeek,
                                   @PageableDefault(size=50, sort="startTime") Pageable p){
        if (dayOfWeek != null) return repo.findByDayOfWeek(dayOfWeek, p);
        return repo.findAll(p);
    }

    @GetMapping("/today")
    public java.util.List<ScheduleItem> today() {
        int dow = java.time.LocalDate.now().getDayOfWeek().getValue(); // 1..7
        return repo.findByDayOfWeekOrderByStartTimeAsc(dow);
    }

    // ===== Crear/Actualizar/Borrar =====
    public record UpsertReq(
            @NotNull Long courseId,
            @Min(1) @Max(7) Integer dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            String room, String color
    ){}

    @PostMapping
    public ResponseEntity<ScheduleItem> create(@Valid @RequestBody UpsertReq req){
        var c = courses.findById(req.courseId()).orElse(null);
        if (c == null) return ResponseEntity.badRequest().build();
        var s = ScheduleItem.builder()
                .course(c).dayOfWeek(req.dayOfWeek())
                .startTime(req.startTime()).endTime(req.endTime())
                .room(req.room()).color(req.color()).build();
        return ResponseEntity.ok(repo.save(s));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ScheduleItem> update(@PathVariable Long id, @RequestBody UpsertReq req){
        return repo.findById(id).map(s -> {
            if (req.courseId()!=null) courses.findById(req.courseId()).ifPresent(s::setCourse);
            if (req.dayOfWeek()!=null) s.setDayOfWeek(req.dayOfWeek());
            if (req.startTime()!=null) s.setStartTime(req.startTime());
            if (req.endTime()!=null) s.setEndTime(req.endTime());
            if (req.room()!=null) s.setRoom(req.room());
            if (req.color()!=null) s.setColor(req.color());
            return ResponseEntity.ok(repo.save(s));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
