package com.kufi.backend.shop;
import com.kufi.backend.user.UserAccount;
import jakarta.persistence.*;
import java.time.LocalTime;
@Entity @Table(name="barber_shops", indexes=@Index(name="idx_shop_district",columnList="district"))
public class BarberShop {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id") public UserAccount owner;
    @Column(nullable=false) public String name;
    @Column(nullable=false) public String city;
    @Column(nullable=false) public String district;
    @Column(nullable=false) public String address;
    @Column(nullable=false) public LocalTime openingTime;
    @Column(nullable=false) public LocalTime closingTime;
    protected BarberShop() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public UserAccount getOwner() { return owner; }
    public BarberShop(UserAccount owner,String name,String city,String district,String address,LocalTime opening,LocalTime closing) {
        this.owner=owner;this.name=name;this.city=city;this.district=district;this.address=address;this.openingTime=opening;this.closingTime=closing;
    }
}
