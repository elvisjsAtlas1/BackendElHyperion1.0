package com.example.elhyperion.activity;

import com.example.elhyperion.course.CourseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityRepository activities;
    private final ReminderRepository reminders;
    private final CourseRepository courses;

    public ActivityController(ActivityRepository activities, ReminderRepository reminders, CourseRepository courses) {
        this.activities = activities;
        this.reminders = reminders;
        this.courses = courses;
    }

    // ====== Listado con filtros ======
    @GetMapping
    public List<Activity> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Activity.Status status,
            @RequestParam(required = false) Instant updatedAfter,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return activities.findAll().stream()
                .filter(a -> courseId == null || a.getCourse().getId().equals(courseId))
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> updatedAfter == null || (a.getUpdatedAt() != null && a.getUpdatedAt().isAfter(updatedAfter)))
                .filter(a -> from == null || (a.getDueDate() != null && !a.getDueDate().isBefore(from)))
                .filter(a -> to == null || (a.getDueDate() != null && !a.getDueDate().isAfter(to)))
                .toList();
    }

    // ====== Crear / actualizar / borrar ======

    public record ActivityReq(
            Long courseId,
            @NotBlank String description,
            LocalDate dueDate,
            LocalTime dueTime,
            Boolean notifyFlag   // <--- antes 'notify'
    ) {}

    @PostMapping
    public ResponseEntity<Activity> create(@Valid @RequestBody ActivityReq req) {
        var courseOpt = courses.findById(req.courseId());
        if (courseOpt.isEmpty()) return ResponseEntity.badRequest().build();

        var a = new Activity();
        a.setCourse(courseOpt.get());
        a.setDescription(req.description());
        a.setDueDate(req.dueDate());
        a.setDueTime(req.dueTime());
        a.setNotify(Boolean.TRUE.equals(req.notifyFlag()));  // <--- aquí
        a.setStatus(Activity.Status.PENDING);
        return ResponseEntity.ok(activities.save(a));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Activity> update(@PathVariable Long id, @RequestBody ActivityReq req) {
        return activities.findById(id).map(a -> {
            if (req.courseId() != null) courses.findById(req.courseId()).ifPresent(a::setCourse);
            if (req.description() != null && !req.description().isBlank()) a.setDescription(req.description());
            if (req.dueDate() != null) a.setDueDate(req.dueDate());
            if (req.dueTime() != null) a.setDueTime(req.dueTime());
            if (req.notifyFlag() != null) a.setNotify(req.notifyFlag());  // <--- aquí
            a.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(activities.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!activities.existsById(id)) return ResponseEntity.notFound().build();
        activities.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ====== Acciones rápidas ======

    @PostMapping("/{id}/done")
    public ResponseEntity<Activity> markDone(@PathVariable Long id) {
        return setStatus(id, Activity.Status.DONE);
    }

    @PostMapping("/{id}/missed")
    public ResponseEntity<Activity> markMissed(@PathVariable Long id) {
        return setStatus(id, Activity.Status.MISSED);
    }

    private ResponseEntity<Activity> setStatus(Long id, Activity.Status st) {
        return activities.findById(id).map(a -> {
            a.setStatus(st);
            a.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(activities.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ====== Reminders (opcional sencillo) ======
    public record ReminderReq(LocalDate date, LocalTime time) {}

    @PostMapping("/{id}/reminders")
    public ResponseEntity<?> addReminder(@PathVariable Long id, @RequestBody ReminderReq req) {
        return activities.findById(id).map(a -> {
            if (req.date() == null || req.time() == null) return ResponseEntity.badRequest().build();
            var r = new Reminder();
            r.setActivity(a);
            r.setRemindAt(OffsetDateTime.of(req.date(), req.time(), ZoneOffset.systemDefault().getRules().getOffset(Instant.now())).toInstant());
            reminders.save(r);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/reminders")
    public ResponseEntity<List<Reminder>> listReminders(@PathVariable Long id) {
        if (!activities.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(reminders.findAll().stream()
                .filter(r -> r.getActivity().getId().equals(id))
                .toList());
    }
}
