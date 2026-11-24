package com.kufi.backend.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kufi.backend.common.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.time.Instant;
import java.util.Map;
@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwt, ObjectMapper mapper) throws Exception {
        http.csrf(csrf->csrf.disable()).httpBasic(b->b.disable()).formLogin(f->f.disable())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shops", "/api/v1/shops/*/services").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/shops", "/api/v1/shops/*/services").hasRole("BARBER")
                .requestMatchers(HttpMethod.POST, "/api/v1/appointments").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/appointments/*/cancel").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/appointments/my", "/api/v1/users/me").authenticated()
                .anyRequest().denyAll())
            .exceptionHandling(e->e
                .authenticationEntryPoint((req,res,ex)->write(mapper,res,401,"Unauthorized","Authentication required",req.getRequestURI()))
                .accessDeniedHandler((req,res,ex)->write(mapper,res,403,"Forbidden","Insufficient permissions",req.getRequestURI())))
            .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    private static void write(ObjectMapper mapper, HttpServletResponse res, int status, String error, String message, String path) throws java.io.IOException {
        res.setStatus(status); res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), new ApiError(Instant.now(), status, error, message, path, Map.of()));
    }
}
