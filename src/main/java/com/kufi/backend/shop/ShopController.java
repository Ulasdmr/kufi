package com.kufi.backend.shop;
import com.kufi.backend.common.ApiException;
import com.kufi.backend.user.UserAccount;
import com.kufi.backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
@RestController @RequestMapping("/api/v1/shops")
public class ShopController {
    private final BarberShopRepository shops; private final ServiceOfferingRepository services; private final UserRepository users;
    public ShopController(BarberShopRepository shops, ServiceOfferingRepository services, UserRepository users) {this.shops=shops;this.services=services;this.users=users;}
    public record ShopRequest(@NotBlank String name,@NotBlank String city,@NotBlank String district,
                              @NotBlank String address,@NotNull LocalTime openingTime,@NotNull LocalTime closingTime) { }
    public record ShopResponse(Long shopId,String name,String city,String district,String address,
                               LocalTime openingTime,LocalTime closingTime) { }
    public record ServiceRequest(@NotBlank String name,@NotNull @Min(1) @Max(480) Integer durationMinutes,
                                 @NotNull @DecimalMin(value="0.0",inclusive=false) BigDecimal price) { }
    public record ServiceResponse(Long serviceId,Long shopId,String name,int durationMinutes,BigDecimal price) { }
    @PostMapping @Transactional
    public ResponseEntity<ShopResponse> create(@Valid @RequestBody ShopRequest req, Authentication auth) {
        if(!req.openingTime().isBefore(req.closingTime())) throw new ApiException(HttpStatus.BAD_REQUEST,"Closing time must be after opening time");
        UserAccount owner=currentUser(auth);
        BarberShop s=shops.save(new BarberShop(owner,req.name(),req.city(),req.district(),req.address(),req.openingTime(),req.closingTime()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toShop(s));
    }
    @GetMapping @Transactional(readOnly=true)
    public List<ShopResponse> list(@RequestParam(required=false) String district) {
        return (district==null || district.isBlank() ? shops.findAll() : shops.findByDistrictIgnoreCase(district)).stream().map(ShopController::toShop).toList();
    }
    @PostMapping("/{shopId}/services") @Transactional
    public ResponseEntity<ServiceResponse> addService(@PathVariable Long shopId,@Valid @RequestBody ServiceRequest req,Authentication auth) {
        BarberShop shop=shops.findById(shopId).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Shop not found"));
        if(!shop.getOwner().getEmail().equalsIgnoreCase(auth.getName())) throw new ApiException(HttpStatus.FORBIDDEN,"You do not own this shop");
        ServiceOffering s=services.save(new ServiceOffering(shop,req.name(),req.durationMinutes(),req.price()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toService(s));
    }
    @GetMapping("/{shopId}/services") @Transactional(readOnly=true)
    public List<ServiceResponse> listServices(@PathVariable Long shopId) {
        if(!shops.existsById(shopId)) throw new ApiException(HttpStatus.NOT_FOUND,"Shop not found");
        return services.findByShopId(shopId).stream().map(ShopController::toService).toList();
    }
    private UserAccount currentUser(Authentication auth) { return users.findByEmailIgnoreCase(auth.getName()).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"User not found")); }
    private static ShopResponse toShop(BarberShop s) {return new ShopResponse(s.id,s.name,s.city,s.district,s.address,s.openingTime,s.closingTime);}
    private static ServiceResponse toService(ServiceOffering s) {return new ServiceResponse(s.getId(),s.getShop().getId(),s.getName(),s.durationMinutes,s.price);}
}
