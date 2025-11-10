package com.example.elhyperion.note;

import com.example.elhyperion.course.CourseRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteRepository notes;
    private final CourseRepository courses;

    public NoteController(NoteRepository notes, CourseRepository courses) {
        this.notes = notes; this.courses = courses;
    }

    @GetMapping
    public Page<Note> list(@RequestParam(required=false) Long courseId,
                           @RequestParam(required=false) String q,
                           @PageableDefault(size=20, sort="updatedAt", direction=Sort.Direction.DESC) Pageable p) {
        if (courseId != null) return notes.findByCourse_Id(courseId, p);
        if (q != null && !q.isBlank()) return notes.findByTitleContainingIgnoreCase(q, p);
        return notes.findAll(p);
    }

    public record UpsertReq(Long courseId, @NotBlank String title, String content) {}

    @PostMapping
    public ResponseEntity<Note> create(@Valid @RequestBody UpsertReq req) {
        var n = Note.builder().title(req.title()).content(req.content()).build();
        if (req.courseId()!=null) courses.findById(req.courseId()).ifPresent(n::setCourse);
        return ResponseEntity.ok(notes.save(n));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Note> update(@PathVariable Long id, @RequestBody UpsertReq req) {
        return notes.findById(id).map(n -> {
            if (req.title()!=null && !req.title().isBlank()) n.setTitle(req.title());
            if (req.content()!=null) n.setContent(req.content());
            if (req.courseId()!=null) courses.findById(req.courseId()).ifPresent(n::setCourse);
            return ResponseEntity.ok(notes.save(n));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!notes.existsById(id)) return ResponseEntity.notFound().build();
        notes.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
