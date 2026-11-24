package com.kufi.backend.user;
import com.kufi.backend.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) {this.users=users;}
    public record MeResponse(Long userId, String email, String role) { }
    @GetMapping("/me") public MeResponse me(Authentication auth) {
        UserAccount u=users.findByEmailIgnoreCase(auth.getName()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found"));
        return new MeResponse(u.id,u.email,u.role.name());
    }
}
