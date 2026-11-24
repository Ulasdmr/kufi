package com.kufi.backend.shop;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="service_offerings")
public class ServiceOffering {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="shop_id") public BarberShop shop;
    @Column(nullable=false) public String name;
    @Column(nullable=false) public int durationMinutes;
    @Column(nullable=false,precision=12,scale=2) public BigDecimal price;
    protected ServiceOffering() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public BarberShop getShop() { return shop; }
    public ServiceOffering(BarberShop shop,String name,int durationMinutes,BigDecimal price) {
        this.shop=shop;this.name=name;this.durationMinutes=durationMinutes;this.price=price;
    }
}
