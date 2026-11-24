package com.kufi.backend.appointment;
import com.kufi.backend.common.ApiException;
import com.kufi.backend.shop.BarberShop;
import com.kufi.backend.shop.BarberShopRepository;
import com.kufi.backend.shop.ServiceOffering;
import com.kufi.backend.shop.ServiceOfferingRepository;
import com.kufi.backend.user.Role;
import com.kufi.backend.user.UserAccount;
import com.kufi.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class AppointmentService {
    private final AppointmentRepository appointments; private final BarberShopRepository shops;
    private final ServiceOfferingRepository services; private final UserRepository users;
    public AppointmentService(AppointmentRepository appointments,BarberShopRepository shops,ServiceOfferingRepository services,UserRepository users) {
        this.appointments=appointments;this.shops=shops;this.services=services;this.users=users;
    }
    public record AppointmentView(Long appointmentId,Long shopId,String shopName,Long serviceId,String serviceName,
                                  LocalDateTime startTime,LocalDateTime endTime,String status) { }
    @Transactional
    public AppointmentView book(Long shopId,Long serviceId,LocalDateTime start,String email) {
        UserAccount customer=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"Account not found"));
        if (customer.role!=Role.CUSTOMER) throw new ApiException(HttpStatus.FORBIDDEN,"Only customers can book");
        // Serialize bookings for the same shop. A naked JPQL overlap check is race-prone.
        BarberShop shop=shops.findByIdForUpdate(shopId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Shop not found"));
        ServiceOffering service=services.findById(serviceId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Service not found"));
        if(!service.getShop().getId().equals(shop.getId())) throw new ApiException(HttpStatus.BAD_REQUEST,"Service does not belong to selected shop");
        if(start==null || !start.isAfter(LocalDateTime.now())) throw new ApiException(HttpStatus.BAD_REQUEST,"Appointment must be in the future");
        LocalDateTime end=BookingTimeRules.calculateEnd(start,service.durationMinutes);
        if(!BookingTimeRules.withinShopHours(start,end,shop.openingTime,shop.closingTime))
            throw new ApiException(HttpStatus.BAD_REQUEST,"Appointment is outside shop operating hours");
        if(appointments.existsOverlappingAppointment(shopId,start,end,AppointmentStatus.CANCELLED))
            throw new ApiException(HttpStatus.CONFLICT,"Time slot conflict: another appointment overlaps");
        Appointment a=appointments.saveAndFlush(new Appointment(customer,shop,service,start,end));
        return map(a);
    }
    @Transactional
    public AppointmentView cancel(Long appointmentId,String email) {
        Appointment a=appointments.findById(appointmentId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Appointment not found"));
        boolean customer=a.customer.getEmail().equalsIgnoreCase(email);
        boolean owner=a.shop.getOwner().getEmail().equalsIgnoreCase(email);
        if(!customer && !owner) throw new ApiException(HttpStatus.FORBIDDEN,"Cannot cancel another user's appointment");
        a.status=AppointmentStatus.CANCELLED;
        return map(a);
    }
    @Transactional(readOnly=true)
    public List<AppointmentView> my(String email) {return appointments.findVisibleTo(email).stream().map(AppointmentService::map).toList();}
    private static AppointmentView map(Appointment a) {
        return new AppointmentView(a.id,a.shop.getId(),a.shop.getName(),a.service.getId(),a.service.getName(),a.startTime,a.endTime,a.status.name());
    }
}
