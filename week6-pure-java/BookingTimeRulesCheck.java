import com.kufi.backend.appointment.BookingTimeRules;
import java.time.LocalDateTime;
import java.time.LocalTime;
/** No Maven needed: javac and java only. Minimal smoke validation, not a substitute for JUnit. */
public class BookingTimeRulesCheck {
    static int passed=0;
    static void check(String id,boolean condition) {
        if(!condition) throw new AssertionError("FAIL "+id);
        System.out.println("PASS "+id);passed++;
    }
    public static void main(String[] args) {
        LocalDateTime start=LocalDateTime.of(2030,5,10,14,0);
        check("S01_calculate_end",BookingTimeRules.calculateEnd(start,45).equals(start.plusMinutes(45)));
        check("S02_partial_overlap",BookingTimeRules.overlaps(start,start.plusMinutes(45),start.plusMinutes(30),start.plusMinutes(60)));
        check("S03_adjacent_non_overlap",!BookingTimeRules.overlaps(start,start.plusMinutes(45),start.plusMinutes(45),start.plusMinutes(90)));
        check("S04_within_shop_hours",BookingTimeRules.withinShopHours(start,start.plusMinutes(45),LocalTime.of(9,0),LocalTime.of(18,0)));
        check("S05_out_of_hours",!BookingTimeRules.withinShopHours(start,start.plusHours(5),LocalTime.of(9,0),LocalTime.of(18,0)));
        try {BookingTimeRules.calculateEnd(start,0);throw new AssertionError("FAIL S06_reject_zero_duration");}
        catch(IllegalArgumentException expected) {check("S06_reject_zero_duration",true);}
        System.out.println("SMOKE TESTS: "+passed+" passed, 0 failed");
    }
}
