import java.util.Map;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Hostile QA Test Suite designed to rigorously stress-test the system across 20 test cases.
 * Refactored for complete Test Isolation, Custom Exception Assertions, and Stream API validation.
 */
public class HostileQATestSuite {
    private static int passedCount = 0;
    private static int failedCount = 0;

    private static BookingManager createManager() {
        return new BookingManager(5, 8); // 40 total seats (A1-E8)
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" 🏴‍☠️ HOSTILE QA STRESS TEST & EVALUATOR AUDIT SUITE (20 SCENARIOS)");
        System.out.println("==========================================================================\n");

        // 1. Double booking
        runTest(1, "Double Booking Prevention", () -> {
            BookingManager manager = createManager();
            manager.bookSeat("A1", "Alice");
            boolean doubleBookResult = manager.bookSeat("A1", "Bob");
            return !doubleBookResult && manager.getSeatCustomerMap().get("A1").equals("Alice") && manager.verifyStateConsistency();
        });

        // 2. Cancelling an available seat
        runTest(2, "Cancelling an Available Seat", () -> {
            BookingManager manager = createManager();
            boolean cancelOpenResult = manager.cancelSeat("B1"); // B1 is open
            return !cancelOpenResult && manager.verifyStateConsistency();
        });

        // 3. Cancelling an invalid seat ID
        runTest(3, "Cancelling an Invalid Seat ID", () -> {
            BookingManager manager = createManager();
            boolean cancelInvalidResult = manager.cancelSeat("INVALID_SEAT");
            return !cancelInvalidResult && manager.verifyStateConsistency();
        });

        // 4. Invalid row
        runTest(4, "Invalid Row Out of Bounds ('Z1')", () -> {
            BookingManager manager = createManager();
            boolean bookResult = manager.bookSeat("Z1", "Dave");
            return !bookResult && manager.verifyStateConsistency();
        });

        // 5. Invalid column
        runTest(5, "Invalid Column Out of Bounds ('A99')", () -> {
            BookingManager manager = createManager();
            boolean bookResult = manager.bookSeat("A99", "Eve");
            return !bookResult && manager.verifyStateConsistency();
        });

        // 6. Invalid seat ID syntax
        runTest(6, "Invalid Seat ID Syntax ('#$@')", () -> {
            BookingManager manager = createManager();
            boolean bookResult = manager.bookSeat("#$@", "Frank");
            return !bookResult && manager.verifyStateConsistency();
        });

        // 7. Empty customer name
        runTest(7, "Empty / Whitespace Customer Name", () -> {
            BookingManager manager = createManager();
            boolean bookResult1 = manager.bookSeat("C1", "");
            boolean bookResult2 = manager.bookSeat("C1", "   ");
            return !bookResult1 && !bookResult2 && manager.verifyStateConsistency();
        });

        // 8. Very long customer name
        runTest(8, "Very Long Customer Name (500+ chars)", () -> {
            BookingManager manager = createManager();
            String longName = "Dr. " + "A".repeat(500) + " von Senior CS Evaluator";
            boolean bookResult = manager.bookSeat("C2", longName);
            boolean found = manager.getSeatCustomerMap().containsKey("C2");
            return bookResult && found && manager.verifyStateConsistency();
        });

        // 9. Rebooking after cancellation
        runTest(9, "Rebooking Seat After Cancellation", () -> {
            BookingManager manager = createManager();
            manager.bookSeat("D1", "Original Customer");
            manager.cancelSeat("D1");
            boolean rebookResult = manager.bookSeat("D1", "New Customer");
            return rebookResult && manager.getSeatCustomerMap().get("D1").equals("New Customer") && manager.verifyStateConsistency();
        });

        // 10. Booking every seat (100% capacity)
        runTest(10, "Booking Every Single Seat (40 / 40)", () -> {
            BookingManager fullManager = createManager();
            for (char r = 'A'; r <= 'E'; r++) {
                for (int c = 1; c <= 8; c++) {
                    String sId = "" + r + c;
                    boolean res = fullManager.bookSeat(sId, "User_" + sId);
                    if (!res) return false;
                }
            }
            return fullManager.getBookedSeatCount() == 40 && fullManager.verifyStateConsistency();
        });

        // 11. Occupancy at 0%
        runTest(11, "Occupancy Percentage at 0% (Empty Theater)", () -> {
            BookingManager emptyManager = createManager();
            return emptyManager.calculateOccupancyPercentage() == 0.0 && emptyManager.verifyStateConsistency();
        });

        // 12. Occupancy at 100%
        runTest(12, "Occupancy Percentage at 100% (Full Theater)", () -> {
            BookingManager fullManager = createManager();
            for (char r = 'A'; r <= 'E'; r++) {
                for (int c = 1; c <= 8; c++) {
                    fullManager.bookSeat("" + r + c, "Guest");
                }
            }
            return Math.abs(fullManager.calculateOccupancyPercentage() - 100.0) < 0.001 && fullManager.verifyStateConsistency();
        });

        // 13. Occupancy after cancellation
        runTest(13, "Occupancy Rate Calculation After Cancellation", () -> {
            BookingManager calcManager = createManager();
            calcManager.bookSeat("A1", "User1");
            calcManager.bookSeat("A2", "User2"); // 2/40 = 5.0%
            calcManager.cancelSeat("A1"); // 1/40 = 2.5%
            return Math.abs(calcManager.calculateOccupancyPercentage() - 2.5) < 0.001 && calcManager.verifyStateConsistency();
        });

        // 14. Searching an existing booking
        runTest(14, "Searching an Existing Booking", () -> {
            BookingManager manager = createManager();
            manager.bookSeat("E5", "Geeta");
            String result = manager.searchBooking("E5");
            return result.contains("Geeta") && result.contains("E5") && manager.verifyStateConsistency();
        });

        // 15. Searching a non-existing booking
        runTest(15, "Searching a Non-Existing Booking", () -> {
            BookingManager manager = createManager();
            String result = manager.searchBooking("E6");
            return result.contains("AVAILABLE") && result.contains("E6") && manager.verifyStateConsistency();
        });

        // 16. Multiple bookings
        runTest(16, "Batch / Multiple Distinct Bookings", () -> {
            BookingManager multiManager = createManager();
            boolean b1 = multiManager.bookSeat("A1", "P1");
            boolean b2 = multiManager.bookSeat("B2", "P2");
            boolean b3 = multiManager.bookSeat("C3", "P3");
            boolean b4 = multiManager.bookSeat("D4", "P4");
            return b1 && b2 && b3 && b4 && multiManager.getBookedSeatCount() == 4 && multiManager.verifyStateConsistency();
        });

        // 17. Collection consistency (SeatMap2D == HashSet == HashMap)
        runTest(17, "Multi-Data-Structure Consistency Assertion", () -> {
            BookingManager manager = createManager();
            manager.bookSeat("A1", "Test");
            manager.cancelSeat("A1");
            manager.bookSeat("B2", "Test2");
            return manager.verifyStateConsistency();
        });

        // 18. Custom Exception Validation: InvalidCustomerException & InvalidSeatException
        runTest(18, "Strict Exception Throwing: Invalid Customer & Seat", () -> {
            BookingManager manager = createManager();
            boolean caughtCustomer = false;
            boolean caughtSeat = false;
            try {
                manager.bookSeatWithException("A1", "");
            } catch (BookingManager.InvalidCustomerException e) {
                caughtCustomer = true;
            }
            try {
                manager.bookSeatWithException("ZZ9", "ValidName");
            } catch (BookingManager.InvalidSeatException e) {
                caughtSeat = true;
            }
            return caughtCustomer && caughtSeat && manager.verifyStateConsistency();
        });

        // 19. Custom Exception Validation: SeatAlreadyBooked & SeatNotBooked
        runTest(19, "Strict Exception Throwing: Double Book & Unbooked Cancel", () -> {
            BookingManager manager = createManager();
            manager.bookSeat("A1", "FirstUser");
            boolean caughtDouble = false;
            boolean caughtUnbooked = false;
            try {
                manager.bookSeatWithException("A1", "SecondUser");
            } catch (BookingManager.SeatAlreadyBookedException e) {
                caughtDouble = true;
            }
            try {
                manager.cancelSeatWithException("E8"); // Not booked
            } catch (BookingManager.SeatNotBookedException e) {
                caughtUnbooked = true;
            }
            return caughtDouble && caughtUnbooked && manager.verifyStateConsistency();
        });

        // 20. Stream API Batch Operations & Filtered Map Invariant
        runTest(20, "Stream API Filter, Sort, and Available Set Invariant", () -> {
            BookingManager manager = createManager();
            IntStream.rangeClosed(1, 5).forEach(i -> manager.bookSeat("A" + i, "VIP_Customer"));
            List<String> sortedBooked = manager.getSortedBookedSeats();
            Map<String, String> vipBookings = manager.searchBookingsByCustomer("VIP");
            List<String> availableSeats = manager.getAllAvailableSeatIds();
            return sortedBooked.size() == 5 && vipBookings.size() == 5 && availableSeats.size() == 35 && manager.verifyStateConsistency();
        });

        System.out.println("\n==========================================================================");
        System.out.println(" QA SUMMARY: " + passedCount + " / 20 PASSED | " + failedCount + " FAILED");
        System.out.println("==========================================================================");
    }

    private static void runTest(int testNum, String description, TestAction action) {
        System.out.printf("Test %02d: %-55s ", testNum, description);
        try {
            boolean success = action.execute();
            if (success) {
                System.out.println("--> [PASS]");
                passedCount++;
            } else {
                System.out.println("--> [FAIL]");
                failedCount++;
            }
        } catch (Exception e) {
            System.out.println("--> [CRASH / EXCEPTION]: " + e.getMessage());
            failedCount++;
        }
    }

    @FunctionalInterface
    interface TestAction {
        boolean execute() throws Exception;
    }
}
