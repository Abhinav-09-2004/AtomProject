/**
 * Automated System Test Suite for the Movie Ticket Booking System.
 *
 * Tests the major functional requirements:
 * - Initial state
 * - Seat booking
 * - Double-booking prevention
 * - Case-insensitive input
 * - Invalid seat IDs
 * - Customer validation
 * - Cancellation
 * - Occupancy calculation
 * - Multi-data-structure consistency
 *
 * Each test creates its own BookingManager to ensure complete test isolation.
 */
public class SystemTestSuite {

    private static BookingManager createManager() {
        return new BookingManager(5, 8); // 40 seats
    }

    public static void main(String[] args) {

        System.out.println("=================================================");
        System.out.println(" 🧪 RUNNING 12-POINT AUTOMATED SYSTEM TEST SUITE");
        System.out.println("=================================================\n");

        int passed = 0;
        int total = 12;

        // -------------------------------------------------
        // TC01: Initial state
        // -------------------------------------------------

        BookingManager tc01 = createManager();

        if (tc01.calculateOccupancyPercentage() == 0.0
                && tc01.getBookedSeatCount() == 0
                && tc01.getSeatCustomerMap().isEmpty()
                && tc01.getBookedSeats().isEmpty()
                && tc01.verifyStateConsistency()) {

            System.out.println(
                    "TC01: Initial State (0% Occupancy) -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC01: Initial State -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC02: Valid single seat booking
        // -------------------------------------------------

        BookingManager tc02 = createManager();

        boolean tc02Booked = tc02.bookSeat("A1", "Alice");

        boolean tc02Customer =
                "Alice".equals(
                        tc02.getSeatCustomerMap().get("A1")
                );

        if (tc02Booked
                && tc02Customer
                && tc02.getBookedSeatCount() == 1
                && tc02.verifyStateConsistency()) {

            System.out.println(
                    "TC02: Valid Single Seat Booking ('A1', 'Alice') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC02: Valid Single Seat Booking -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC03: Prevent double booking
        // -------------------------------------------------

        BookingManager tc03 = createManager();

        tc03.bookSeat("A1", "Alice");

        boolean secondBooking =
                tc03.bookSeat("A1", "Bob");

        boolean originalCustomer =
                "Alice".equals(
                        tc03.getSeatCustomerMap().get("A1")
                );

        if (!secondBooking
                && originalCustomer
                && tc03.getBookedSeatCount() == 1
                && tc03.verifyStateConsistency()) {

            System.out.println(
                    "TC03: Prevent Double Booking ('A1' twice) -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC03: Prevent Double Booking -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC04: Case-insensitive seat input
        // -------------------------------------------------

        BookingManager tc04 = createManager();

        boolean booked =
                tc04.bookSeat("a2", "Charlie");

        boolean normalized =
                tc04.getSeatCustomerMap().containsKey("A2");

        if (booked
                && normalized
                && "Charlie".equals(
                        tc04.getSeatCustomerMap().get("A2"))
                && tc04.verifyStateConsistency()) {

            System.out.println(
                    "TC04: Case Insensitive Input ('a2' -> 'A2') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC04: Case Insensitive Input -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC05: Invalid row
        // -------------------------------------------------

        BookingManager tc05 = createManager();

        boolean invalidRow =
                tc05.bookSeat("Z1", "Dave");

        if (!invalidRow
                && tc05.getBookedSeatCount() == 0
                && tc05.getBookedSeats().isEmpty()
                && tc05.getSeatCustomerMap().isEmpty()
                && tc05.verifyStateConsistency()) {

            System.out.println(
                    "TC05: Out of Bounds Row ('Z1') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC05: Out of Bounds Row -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC06: Invalid column
        // -------------------------------------------------

        BookingManager tc06 = createManager();

        boolean invalidColumn =
                tc06.bookSeat("A15", "Eve");

        if (!invalidColumn
                && tc06.getBookedSeatCount() == 0
                && tc06.verifyStateConsistency()) {

            System.out.println(
                    "TC06: Out of Bounds Column ('A15') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC06: Out of Bounds Column -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC07: Invalid syntax
        // -------------------------------------------------

        BookingManager tc07 = createManager();

        boolean invalidSyntax =
                tc07.bookSeat("ABC", "Frank");

        if (!invalidSyntax
                && tc07.getBookedSeatCount() == 0
                && tc07.verifyStateConsistency()) {

            System.out.println(
                    "TC07: Invalid Syntax Seat ID ('ABC') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC07: Invalid Syntax Seat ID -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC08: Blank customer name
        // -------------------------------------------------

        BookingManager tc08 = createManager();

        boolean blankCustomer =
                tc08.bookSeat("B2", "   ");

        if (!blankCustomer
                && tc08.getBookedSeatCount() == 0
                && tc08.getSeatCustomerMap().isEmpty()
                && tc08.verifyStateConsistency()) {

            System.out.println(
                    "TC08: Blank Customer Name Validation -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC08: Blank Customer Name -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC09: Valid cancellation
        // -------------------------------------------------

        BookingManager tc09 = createManager();

        tc09.bookSeat("A1", "Alice");

        boolean cancelled =
                tc09.cancelSeat("A1");

        boolean removedFromMap =
                !tc09.getSeatCustomerMap().containsKey("A1");

        boolean removedFromSet =
                !tc09.getBookedSeats().contains("A1");

        boolean availableAgain =
                !tc09.getSeatMap().isBooked(0, 0);

        if (cancelled
                && removedFromMap
                && removedFromSet
                && availableAgain
                && tc09.getBookedSeatCount() == 0
                && tc09.verifyStateConsistency()) {

            System.out.println(
                    "TC09: Valid Seat Cancellation ('A1') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC09: Valid Seat Cancellation -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC10: Cancel non-booked seat
        // -------------------------------------------------

        BookingManager tc10 = createManager();

        boolean cancelledUnbooked =
                tc10.cancelSeat("C3");

        if (!cancelledUnbooked
                && tc10.getBookedSeatCount() == 0
                && tc10.verifyStateConsistency()) {

            System.out.println(
                    "TC10: Cancel Non-Booked Seat ('C3') -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC10: Cancel Non-Booked Seat -> FAILED"
            );
        }

        // -------------------------------------------------
        // TC11: Occupancy calculation
        // -------------------------------------------------

        BookingManager tc11 = createManager();

        String[] seats = {
                "A1",
                "B1",
                "B2",
                "B3",
                "B4",
                "C1",
                "C2",
                "C3",
                "C4",
                "D1"
        };

        for (String seat : seats) {
            tc11.bookSeat(seat, "Guest");
        }

        double occupancy =
                tc11.calculateOccupancyPercentage();

        if (tc11.getBookedSeatCount() == 10
                && Math.abs(occupancy - 25.0) < 0.001
                && tc11.verifyStateConsistency()) {

            System.out.println(
                    "TC11: Occupancy Calculation (10/40 = 25.00%) -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC11: Occupancy Calculation -> FAILED"
            );

            System.out.println(
                    "      Actual occupancy: "
                            + occupancy
                            + "%"
            );
        }

        // -------------------------------------------------
        // TC12: Full consistency check
        // -------------------------------------------------

        BookingManager tc12 = createManager();

        tc12.bookSeat("A1", "Test1");
        tc12.bookSeat("E8", "Test2");

        tc12.cancelSeat("A1");

        boolean correctFinalState =
                tc12.getBookedSeatCount() == 1
                && tc12.getBookedSeats().contains("E8")
                && !tc12.getBookedSeats().contains("A1")
                && tc12.getSeatCustomerMap().containsKey("E8")
                && !tc12.getSeatCustomerMap().containsKey("A1")
                && tc12.getSeatMap().isBooked(4, 7)
                && !tc12.getSeatMap().isBooked(0, 0);

        if (correctFinalState
                && tc12.verifyStateConsistency()) {

            System.out.println(
                    "TC12: Full Multi-Data-Structure Consistency Check -> PASSED"
            );
            passed++;

        } else {

            System.out.println(
                    "TC12: Consistency Check -> FAILED"
            );
        }

        // -------------------------------------------------
        // Final Results
        // -------------------------------------------------

        double passPercentage =
                (passed * 100.0) / total;

        System.out.println("\n=================================================");
        System.out.println(
                " TEST RESULTS: "
                        + passed
                        + " / "
                        + total
                        + " PASSED"
        );

        System.out.printf(
                " PASS PERCENTAGE: %.2f%%%n",
                passPercentage
        );

        if (passed == total) {
            System.out.println(
                    " STATUS: ALL SYSTEM TESTS PASSED ✅"
            );
        } else {
            System.out.println(
                    " STATUS: SOME TESTS FAILED ❌"
            );
        }

        System.out.println("=================================================");
    }
}