package com.kufi.backend.appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    @Query("select case when count(a)>0 then true else false end from Appointment a where a.shop.id=:shopId and a.status<>:cancelled and a.startTime<:endTime and a.endTime>:startTime")
    boolean existsOverlappingAppointment(@Param("shopId") Long shopId,@Param("startTime") LocalDateTime start,
            @Param("endTime") LocalDateTime end,@Param("cancelled") AppointmentStatus cancelled);
    @Query("select a from Appointment a where a.customer.email=:email or a.shop.owner.email=:email order by a.startTime desc")
    List<Appointment> findVisibleTo(@Param("email") String email);
}
