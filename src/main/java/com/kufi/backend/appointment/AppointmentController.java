package com.kufi.backend.appointment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
@RestController @RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService service;
    public AppointmentController(AppointmentService service) {this.service=service;}
    public record BookRequest(@NotNull Long shopId,@NotNull Long serviceId,@NotNull @Future LocalDateTime startTime) { }
    @PostMapping
    public ResponseEntity<AppointmentService.AppointmentView> book(@Valid @RequestBody BookRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.book(req.shopId(),req.serviceId(),req.startTime(),auth.getName()));
    }
    @PatchMapping("/{id}/cancel")
    public AppointmentService.AppointmentView cancel(@PathVariable Long id,Authentication auth) {return service.cancel(id,auth.getName());}
    @GetMapping("/my")
    public List<AppointmentService.AppointmentView> my(Authentication auth) {return service.my(auth.getName());}
}
