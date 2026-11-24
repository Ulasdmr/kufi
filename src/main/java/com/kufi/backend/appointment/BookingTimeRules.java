package com.kufi.backend.appointment;
import java.time.LocalDateTime;
import java.time.LocalTime;
/** Pure, deterministic domain rules. Kept outside Spring so Week 6 can test them with javac. */
public final class BookingTimeRules {
    private BookingTimeRules() { }
    public static LocalDateTime calculateEnd(LocalDateTime start, int durationMinutes) {
        if (start==null || durationMinutes<=0 || durationMinutes>480) throw new IllegalArgumentException("Invalid start or duration");
        return start.plusMinutes(durationMinutes);
    }
    /** Half-open intervals: [start,end). Adjacent appointments do not overlap. */
    public static boolean overlaps(LocalDateTime existingStart,LocalDateTime existingEnd,
                                   LocalDateTime requestedStart,LocalDateTime requestedEnd) {
        return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
    }
    public static boolean withinShopHours(LocalDateTime start,LocalDateTime end,LocalTime open,LocalTime close) {
        return start.toLocalDate().equals(end.toLocalDate()) && !start.toLocalTime().isBefore(open)
                && !end.toLocalTime().isAfter(close) && open.isBefore(close);
    }
}
