package com.kufi.backend.shop;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface BarberShopRepository extends JpaRepository<BarberShop,Long> {
    List<BarberShop> findByDistrictIgnoreCase(String district);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from BarberShop s where s.id = :id")
    Optional<BarberShop> findByIdForUpdate(@Param("id") Long id);
}
