package com.kufi.backend;
import com.kufi.backend.appointment.*;
import com.kufi.backend.common.ApiException;
import com.kufi.backend.shop.*;
import com.kufi.backend.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ConcurrentBookingTest {
    @Autowired AppointmentService booking;
    @Autowired AppointmentRepository appointments;
    @Autowired ServiceOfferingRepository services;
    @Autowired BarberShopRepository shops;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Test void T13_twoSimultaneousRequestsAcceptExactlyOne() throws Exception {
        // New unique data per run; the two competing requests use a single shop lock.
        String id=java.util.UUID.randomUUID().toString().substring(0,8);
        UserAccount owner=users.save(new UserAccount("owner-"+id+"@test.local",passwords.encode("TestPassword123!"),"000",Role.BARBER));
        UserAccount one=users.save(new UserAccount("one-"+id+"@test.local",passwords.encode("TestPassword123!"),"000",Role.CUSTOMER));
        UserAccount two=users.save(new UserAccount("two-"+id+"@test.local",passwords.encode("TestPassword123!"),"000",Role.CUSTOMER));
        BarberShop shop=shops.save(new BarberShop(owner,"Concurrency test","Istanbul","Test","Test Street",LocalTime.of(9,0),LocalTime.of(18,0)));
        ServiceOffering service=services.save(new ServiceOffering(shop,"Test haircut",45,new BigDecimal("300.00")));
        var start=LocalDate.now().plusDays(20).atTime(14,0);
        CountDownLatch ready=new CountDownLatch(2),go=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        Callable<Boolean> task1=() -> attempt(shop.id,service.id,start,one.email,ready,go);
        Callable<Boolean> task2=() -> attempt(shop.id,service.id,start,two.email,ready,go);
        try {
            Future<Boolean> a=pool.submit(task1), b=pool.submit(task2);
            assertTrue(ready.await(10,TimeUnit.SECONDS));
            go.countDown();
            int successful=(a.get(30,TimeUnit.SECONDS)?1:0)+(b.get(30,TimeUnit.SECONDS)?1:0);
            assertEquals(1,successful,"Exactly one conflicting booking must succeed");
            assertTrue(appointments.existsOverlappingAppointment(shop.id,start,start.plusMinutes(45),AppointmentStatus.CANCELLED));
        } finally {go.countDown();pool.shutdownNow();}
    }
    private boolean attempt(Long shopId,Long serviceId,java.time.LocalDateTime start,String email,CountDownLatch ready,CountDownLatch go) throws Exception {
        ready.countDown();go.await(10,TimeUnit.SECONDS);
        try {booking.book(shopId,serviceId,start,email);return true;}
        catch(ApiException ex) {if(ex.getStatus().value()==409) return false;throw ex;}
    }
}
