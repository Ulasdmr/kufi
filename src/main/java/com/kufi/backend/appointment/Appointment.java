package com.kufi.backend.appointment;
import com.kufi.backend.shop.BarberShop;
import com.kufi.backend.shop.ServiceOffering;
import com.kufi.backend.user.UserAccount;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="appointments",indexes=@Index(name="idx_appt_shop_time",columnList="shop_id,start_time"))
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="customer_id") public UserAccount customer;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shop_id") public BarberShop shop;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="service_id") public ServiceOffering service;
    @Column(nullable=false) public LocalDateTime startTime;
    @Column(nullable=false) public LocalDateTime endTime;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public AppointmentStatus status;
    protected Appointment() {}
    public Appointment(UserAccount customer,BarberShop shop,ServiceOffering service,LocalDateTime start,LocalDateTime end) {
        this.customer=customer;this.shop=shop;this.service=service;this.startTime=start;this.endTime=end;this.status=AppointmentStatus.CONFIRMED;
    }
}
