package com.example.elhyperion.personal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;

@RestController
@RequestMapping("/personal/tasks")
public class PersonalTaskController {

    private final PersonalTaskRepository repo;

    public PersonalTaskController(PersonalTaskRepository repo) { this.repo = repo; }

    // ===== Listas =====
    @GetMapping
    public Page<PersonalTask> list(@RequestParam(required=false) PersonalTask.Status status,
                                   @RequestParam(required=false) LocalDate from,
                                   @RequestParam(required=false) LocalDate to,
                                   @PageableDefault(size=20, sort="dueDate") Pageable p){
        if (status != null) return repo.findByStatus(status, p);
        if (from!=null && to!=null) return repo.findByDueDateBetween(from, to, p);
        return repo.findAll(p);
    }

    @GetMapping("/upcoming")
    public java.util.List<PersonalTask> upcoming(){
        return repo.findTop20ByStatusOrderByDueDateAsc(PersonalTask.Status.PENDING);
    }

    // ===== CRUD =====
    public record UpsertReq(
            @NotBlank String description,
            LocalDate dueDate,
            LocalTime dueTime,
            Boolean notifyFlag   // <-- antes Boolean notify
    ) {}

    @PostMapping
    public PersonalTask create(@Valid @RequestBody UpsertReq req){
        return repo.save(PersonalTask.builder()
                .description(req.description())
                .dueDate(req.dueDate())
                .dueTime(req.dueTime())
                .notify(Boolean.TRUE.equals(req.notifyFlag()))   // <-- usa notifyFlag()
                .status(PersonalTask.Status.PENDING)
                .build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PersonalTask> update(@PathVariable Long id, @RequestBody UpsertReq req){
        return repo.findById(id).map(t -> {
            if (req.description()!=null && !req.description().isBlank()) t.setDescription(req.description());
            if (req.dueDate()!=null) t.setDueDate(req.dueDate());
            if (req.dueTime()!=null) t.setDueTime(req.dueTime());
            if (req.notifyFlag()!=null) t.setNotify(req.notifyFlag());  // <-- usa notifyFlag()
            return ResponseEntity.ok(repo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Acciones rápidas
    @PostMapping("/{id}/done")
    public ResponseEntity<PersonalTask> markDone(@PathVariable Long id){
        return setStatus(id, PersonalTask.Status.DONE);
    }

    @PostMapping("/{id}/missed")
    public ResponseEntity<PersonalTask> markMissed(@PathVariable Long id){
        return setStatus(id, PersonalTask.Status.MISSED);
    }

    private ResponseEntity<PersonalTask> setStatus(Long id, PersonalTask.Status st){
        return repo.findById(id).map(t -> {
            t.setStatus(st);
            t.setUpdatedAt(java.time.Instant.now());
            return ResponseEntity.ok(repo.save(t));
        }).orElse(ResponseEntity.notFound().build());
    }
}
