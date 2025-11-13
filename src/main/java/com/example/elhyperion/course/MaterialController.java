package com.example.elhyperion.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialRepository materials;
    private final CourseRepository courses;
    private final CourseWeekRepository weeks;

    public MaterialController(MaterialRepository materials, CourseRepository courses, CourseWeekRepository weeks) {
        this.materials = materials;
        this.courses = courses;
        this.weeks = weeks;
    }

    @GetMapping
    public List<Material> list(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long weekId,
            @RequestParam(required = false) Instant updatedAfter
    ) {
        // En simple: filtramos en memoria. Puedes moverlo a queries JPA si quieres.
        return materials.findAll().stream()
                .filter(m -> courseId == null || m.getCourse().getId().equals(courseId))
                .filter(m -> weekId == null || (m.getWeek() != null && m.getWeek().getId().equals(weekId)))
                .filter(m -> updatedAfter == null || (m.getUpdatedAt() != null && m.getUpdatedAt().isAfter(updatedAfter)))
                .toList();
    }

    public record MaterialReq(
            Long courseId,
            Long weekId,
            @NotBlank String title,
            Material.Type type,
            String infoText,
            String url,
            String mime
    ) {}

    @PostMapping
    public ResponseEntity<Material> create(@Valid @RequestBody MaterialReq req) {
        var courseOpt = courses.findById(req.courseId());
        if (courseOpt.isEmpty()) return ResponseEntity.badRequest().build();

        var m = new Material();
        m.setCourse(courseOpt.get());
        if (req.weekId() != null) {
            weeks.findById(req.weekId()).ifPresent(m::setWeek);
        }
        m.setTitle(req.title());
        m.setType(req.type() == null ? Material.Type.TEXT : req.type());
        m.setInfoText(req.infoText());
        m.setUrl(req.url());
        m.setMime(req.mime());
        return ResponseEntity.ok(materials.save(m));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Material> update(@PathVariable Long id, @RequestBody MaterialReq req) {
        return materials.findById(id).map(m -> {
            if (req.title() != null && !req.title().isBlank()) m.setTitle(req.title());
            if (req.type() != null) m.setType(req.type());
            if (req.infoText() != null) m.setInfoText(req.infoText());
            if (req.url() != null) m.setUrl(req.url());
            if (req.mime() != null) m.setMime(req.mime());
            if (req.courseId() != null) courses.findById(req.courseId()).ifPresent(m::setCourse);
            if (req.weekId() != null) {
                if (req.weekId() == -1) m.setWeek(null);  // quitar semana
                else weeks.findById(req.weekId()).ifPresent(m::setWeek);
            }
            m.setUpdatedAt(Instant.now());
            return ResponseEntity.ok(materials.save(m));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!materials.existsById(id)) return ResponseEntity.notFound().build();
        materials.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Material> upload(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long weekId,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            var courseOpt = courses.findById(courseId);
            if (courseOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Carpeta donde se guardan los archivos
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(storedName);

            // Guardar en disco
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String mime = file.getContentType();

            Material.Type type;
            if (mime != null && mime.startsWith("image/")) {
                type = Material.Type.IMG;
            } else {
                type = Material.Type.DOC;
            }

            // URL pública que usará la app
            String url = "/files/" + storedName;

            var m = new Material();
            m.setCourse(courseOpt.get());
            if (weekId != null) {
                weeks.findById(weekId).ifPresent(m::setWeek);
            }
            m.setTitle(original != null ? original : storedName);
            m.setType(type);
            m.setInfoText(null);
            m.setUrl(url);
            m.setMime(mime);

            Material saved = materials.save(m);
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
