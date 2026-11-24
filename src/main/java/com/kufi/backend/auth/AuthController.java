package com.kufi.backend.auth;
import com.kufi.backend.common.ApiException;
import com.kufi.backend.security.JwtService;
import com.kufi.backend.user.Role;
import com.kufi.backend.user.UserAccount;
import com.kufi.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt;
    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) { this.users=users; this.encoder=encoder; this.jwt=jwt; }
    public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8) String password,
                                  @NotBlank String phone, @NotNull Role role) { }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
    public record AuthResponse(Long userId, String email, String role, String accessToken) { }
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        String email=req.email().trim().toLowerCase(Locale.ROOT);
        if(users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT,"Email is already registered");
        UserAccount user=users.saveAndFlush(new UserAccount(email,encoder.encode(req.password()),req.phone(),req.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(user.id,user.email,user.role.name(),jwt.create(user.email,user.role.name())));
    }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        UserAccount user=users.findByEmailIgnoreCase(req.email()).orElse(null);
        if(user==null || !encoder.matches(req.password(),user.passwordHash))
            throw new ApiException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
        return new AuthResponse(user.id,user.email,user.role.name(),jwt.create(user.email,user.role.name()));
    }
}
