package com.kufi.backend.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering,Long> {
    List<ServiceOffering> findByShopId(Long shopId);
}
