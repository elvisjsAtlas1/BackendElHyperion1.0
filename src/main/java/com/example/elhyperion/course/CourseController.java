package com.example.elhyperion.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courses;
    private final CourseWeekRepository weeks;

    public CourseController(CourseRepository courses, CourseWeekRepository weeks) {
        this.courses = courses;
        this.weeks = weeks;
    }

    // ====== Courses ======

    @GetMapping
    public List<Course> list() {
        return courses.findAll();
    }

    public record CourseReq(@NotBlank String name, String color, String description) {}

    @PostMapping
    public Course create(@Valid @RequestBody CourseReq req) {
        var c = new Course();
        c.setName(req.name());
        c.setColor(req.color());
        c.setDescription(req.description());
        return courses.save(c);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Course> update(@PathVariable Long id, @RequestBody CourseReq req) {
        return courses.findById(id)
                .map(c -> {
                    if (req.name() != null && !req.name().isBlank()) c.setName(req.name());
                    if (req.color() != null) c.setColor(req.color());
                    if (req.description() != null) c.setDescription(req.description());
                    c.setUpdatedAt(Instant.now());
                    return ResponseEntity.ok(courses.save(c));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!courses.existsById(id)) return ResponseEntity.notFound().build();
        courses.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ====== Weeks por curso ======

    @GetMapping("/{courseId}/weeks")
    public List<CourseWeek> listWeeks(@PathVariable Long courseId) {
        // sencillo: devuelve todas las semanas y deja al front ordenarlas por number
        return weeks.findAll().stream().filter(w -> w.getCourse().getId().equals(courseId)).toList();
    }

    public record WeekReq(Integer number, String title) {}

    @PostMapping("/{courseId}/weeks")
    public ResponseEntity<CourseWeek> addWeek(@PathVariable Long courseId, @RequestBody WeekReq req) {
        return courses.findById(courseId)
                .map(course -> {
                    var w = new CourseWeek();
                    w.setCourse(course);
                    w.setNumber(req.number() == null ? 1 : req.number());
                    w.setTitle(req.title());
                    return ResponseEntity.ok(weeks.save(w));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
