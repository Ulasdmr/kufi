package com.kufi.backend.appointment;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;
class BookingTimeRulesTest {
    final LocalDateTime at14=LocalDateTime.of(2030,5,10,14,0);
    @Test void T01_calculates45MinuteEnd() {
        assertEquals(LocalDateTime.of(2030,5,10,14,45), BookingTimeRules.calculateEnd(at14,45));
    }
    @Test void T02_partialOverlapIsTrue() {
        assertTrue(BookingTimeRules.overlaps(at14,at14.plusMinutes(45),at14.plusMinutes(30),at14.plusMinutes(60)));
    }
    @Test void T03_adjacentSlotsDoNotOverlap() {
        assertFalse(BookingTimeRules.overlaps(at14,at14.plusMinutes(45),at14.plusMinutes(45),at14.plusMinutes(90)));
    }
    @Test void T04_operatingHoursBoundary() {
        assertTrue(BookingTimeRules.withinShopHours(at14,at14.plusMinutes(45),LocalTime.of(9,0),LocalTime.of(18,0)));
        assertFalse(BookingTimeRules.withinShopHours(at14,at14.plusHours(5),LocalTime.of(9,0),LocalTime.of(18,0)));
    }
    @Test void T05_invalidDurationRejected() {
        assertThrows(IllegalArgumentException.class, () -> BookingTimeRules.calculateEnd(at14,0));
    }
}
