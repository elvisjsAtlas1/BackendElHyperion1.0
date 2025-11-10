package com.example.elhyperion.auth;

import com.example.elhyperion.security.JwtService;
import com.example.elhyperion.user.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(AuthenticationManager authManager, JwtService jwtService,
                          UserRepository users, PasswordEncoder encoder) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.users = users;
        this.encoder = encoder;
    }

    public record LoginReq(@NotBlank String email, @NotBlank String password) {}
    public record TokenRes(String token, String role) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        var auth = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        authManager.authenticate(auth); // lanza excepción si credenciales malas
        var u = users.findByEmail(req.email()).orElseThrow();
        String token = jwtService.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return ResponseEntity.ok(new TokenRes(token, u.getRole().name()));
    }

    public record RegisterFirstAdminReq(@NotBlank String email, @NotBlank String password) {}

    /**
     * Crea el PRIMER admin si aún no existe ninguno. Úsalo una sola vez.
     */
    @PostMapping("/register-first-admin")
    public ResponseEntity<?> registerFirstAdmin(@RequestBody RegisterFirstAdminReq req) {
        boolean hasAdmin = users.findAll().stream().anyMatch(u -> u.getRole() == Role.ADMIN);
        if (hasAdmin) return ResponseEntity.status(403).body(Map.of("error", "Admin already exists"));
        if (users.existsByEmail(req.email())) return ResponseEntity.badRequest().body(Map.of("error", "Email exists"));

        var u = User.builder()
                .email(req.email())
                .passwordHash(encoder.encode(req.password()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        users.save(u);
        return ResponseEntity.ok(Map.of("created", "ok"));
    }
}
