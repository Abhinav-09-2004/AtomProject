import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BookingManager handles movie ticket booking business logic.
 * 
 * Coordinates three data structures to ensure state consistency:
 *   1. Custom SeatMap2D (physical layout matrix grid)
 *   2. HashSet<String> bookedSeats (O(1) fast uniqueness and availability lookup)
 *   3. HashMap<String, String> seatCustomerMap (O(1) seat ID -> customer name relation)
 */
public class BookingManager {
    private final SeatMap2D seatMap;
    private final HashSet<String> bookedSeats;
    private final HashMap<String, String> seatCustomerMap;

    /**
     * Constructs a BookingManager with given theater rows and columns.
     * 
     * @param rows seating rows count (1-26)
     * @param cols seating columns count (>0)
     */
    public BookingManager(int rows, int cols) {
        this.seatMap = new SeatMap2D(rows, cols);
        this.bookedSeats = new HashSet<>();
        this.seatCustomerMap = new HashMap<>();
    }

    /**
     * Returns reference to the underlying SeatMap2D.
     */
    public SeatMap2D getSeatMap() {
        return seatMap;
    }

    /**
     * Normalizes seat ID by trimming whitespace and converting to uppercase.
     */
    private String normalizeSeatId(String seatId) {
        return seatId == null ? "" : seatId.trim().toUpperCase();
    }

    /**
     * Books a seat for a given customer.
     * 
     * Maintains strict consistency across SeatMap2D, HashSet, and HashMap.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "b3")
     * @param customerName Name of booking customer
     * @return true if booking succeeded, false if invalid seat ID, empty name, or double booking
     */
    public boolean bookSeat(String seatId, String customerName) {
        // 1. Validate customer name
        if (customerName == null || customerName.trim().isEmpty()) {
            System.out.println("❌ Booking Error: Customer name cannot be empty.");
            return false;
        }

        // 2. Normalize seat ID
        String cleanSeatId = normalizeSeatId(seatId);

        // 3. Parse and validate seat ID format & grid boundaries
        int[] coords = seatMap.parseSeatId(cleanSeatId);
        if (coords == null) {
            System.out.println("❌ Booking Error: Seat ID '" + seatId + "' is invalid or out of bounds.");
            return false;
        }

        int row = coords[0];
        int col = coords[1];

        // 4. Prevent double booking using HashSet O(1) lookup
        if (bookedSeats.contains(cleanSeatId)) {
            System.out.println("❌ Double Booking Alert: Seat " + cleanSeatId + " is already booked!");
            return false;
        }

        // 5. Update custom 2D matrix
        boolean matrixSuccess = seatMap.bookSeat(row, col);
        if (!matrixSuccess) {
            System.out.println("❌ Booking Error: Matrix cell status conflict for " + cleanSeatId);
            return false;
        }

        // 6. Update HashSet and HashMap to keep state synchronized
        String cleanName = customerName.trim();
        bookedSeats.add(cleanSeatId);
        seatCustomerMap.put(cleanSeatId, cleanName);

        System.out.println("✅ Success: Seat " + cleanSeatId + " successfully booked for " + cleanName + ".");
        return true;
    }

    /**
     * Cancels an existing seat booking.
     * 
     * Removes seat from SeatMap2D, HashSet, and HashMap to maintain consistency.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "B3")
     * @return true if cancellation succeeded, false if invalid seat or seat not booked
     */
    public boolean cancelSeat(String seatId) {
        // 1. Normalize seat ID
        String cleanSeatId = normalizeSeatId(seatId);

        // 2. Parse and validate seat ID format & grid boundaries
        int[] coords = seatMap.parseSeatId(cleanSeatId);
        if (coords == null) {
            System.out.println("❌ Cancellation Error: Seat ID '" + seatId + "' is invalid or out of bounds.");
            return false;
        }

        int row = coords[0];
        int col = coords[1];

        // 3. Check if seat is currently booked using HashSet O(1) lookup
        if (!bookedSeats.contains(cleanSeatId)) {
            System.out.println("❌ Cancellation Error: Seat " + cleanSeatId + " is not currently booked!");
            return false;
        }

        // 4. Update custom 2D matrix
        boolean matrixSuccess = seatMap.cancelSeat(row, col);
        if (!matrixSuccess) {
            System.out.println("❌ Cancellation Error: Matrix cell status conflict for " + cleanSeatId);
            return false;
        }

        // 5. Update HashSet and HashMap to maintain consistency
        String customerName = seatCustomerMap.remove(cleanSeatId);
        bookedSeats.remove(cleanSeatId);

        System.out.println("✅ Success: Booking for seat " + cleanSeatId + " (" + customerName + ") has been cancelled.");
        return true;
    }

    /**
     * Searches for booking details associated with a given seat ID.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1")
     * @return Customer name if seat is booked, or descriptive status string if available/invalid
     */
    public String searchBooking(String seatId) {
        String cleanSeatId = normalizeSeatId(seatId);
        int[] coords = seatMap.parseSeatId(cleanSeatId);

        if (coords == null) {
            return "Invalid seat ID or out of bounds.";
        }

        if (bookedSeats.contains(cleanSeatId)) {
            String customer = seatCustomerMap.get(cleanSeatId);
            return "Seat " + cleanSeatId + " is booked by: " + customer;
        } else {
            return "Seat " + cleanSeatId + " is currently AVAILABLE.";
        }
    }

    /**
     * Returns total number of currently booked seats using HashSet.size().
     */
    public int getBookedSeatCount() {
        return bookedSeats.size();
    }

    /**
     * Calculates the occupancy percentage of the theater.
     */
    public double calculateOccupancyPercentage() {
        if (seatMap.getTotalSeats() == 0) {
            return 0.0;
        }
        return (bookedSeats.size() * 100.0) / seatMap.getTotalSeats();
    }

    /**
     * Displays current seat layout matrix.
     */
    public void displaySeatMap() {
        seatMap.printMap();
    }

    /**
     * Returns unmodifiable view of booked seat IDs set.
     */
    public Set<String> getBookedSeats() {
        return Collections.unmodifiableSet(bookedSeats);
    }

    /**
     * Returns unmodifiable view of seat customer map.
     */
    public Map<String, String> getSeatCustomerMap() {
        return Collections.unmodifiableMap(seatCustomerMap);
    }

    /**
     * Verifies internal consistency across SeatMap2D, HashSet, and HashMap.
     * 
     * @return true if all 3 data structures are in 100% synchronized state
     */
    public boolean verifyStateConsistency() {
        int hashSetSize = bookedSeats.size();
        int hashMapSize = seatCustomerMap.size();
        int matrixCount = seatMap.getBookedCount();

        if (hashSetSize != hashMapSize || hashSetSize != matrixCount) {
            return false;
        }

        // Verify every seat in HashSet is marked 'X' in Matrix and present in HashMap
        for (String seatId : bookedSeats) {
            if (!seatCustomerMap.containsKey(seatId)) {
                return false;
            }
            int[] coords = seatMap.parseSeatId(seatId);
            if (coords == null || !seatMap.isBooked(coords[0], coords[1])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Standalone main method for Phase 2 integration & consistency verification.
     */
    public static void main(String[] args) {
        System.out.println("--- Phase 2: Testing BookingManager & Collections Integration ---");

        BookingManager manager = new BookingManager(5, 8);
        System.out.println("Initialized BookingManager with 5x8 grid (40 total seats).");

        // 1. Initial State Check
        System.out.println("\n1. Initial Consistency Check: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));

        // 2. Successful Booking
        System.out.println("\n2. Booking seats:");
        manager.bookSeat("A1", "Rahul Sharma");
        manager.bookSeat("b3", "Priya Singh");
        manager.bookSeat("E8", "Amit Kumar");

        System.out.println("Consistency Check after 3 bookings: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));
        System.out.println("Booked seats count: " + manager.getBookedSeatCount());
        System.out.printf("Occupancy percentage: %.2f%%\n", manager.calculateOccupancyPercentage());

        // 3. Prevent Double Booking
        System.out.println("\n3. Testing Double Booking Prevention:");
        boolean doubleBookResult = manager.bookSeat("A1", "Vikram Rathore");
        System.out.println("Double booking result: " + (!doubleBookResult ? "PASSED (Prevented)" : "FAILED"));
        System.out.println("Consistency Check after double booking attempt: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));

        // 4. Testing Invalid Inputs
        System.out.println("\n4. Testing Edge Cases & Invalid Inputs:");
        manager.bookSeat("Z1", "John");      // Out of row bounds
        manager.bookSeat("A15", "Jane");     // Out of col bounds
        manager.bookSeat("ABC", "Mark");     // Invalid syntax
        manager.bookSeat("B2", "   ");       // Empty name check

        System.out.println("Consistency Check after bad inputs: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));

        // 5. Search Bookings
        System.out.println("\n5. Testing Search Booking:");
        System.out.println("Search A1: " + manager.searchBooking("A1"));
        System.out.println("Search B3: " + manager.searchBooking("b3"));
        System.out.println("Search C5 (Available): " + manager.searchBooking("C5"));

        // 6. Seat Cancellation
        System.out.println("\n6. Testing Cancellation:");
        manager.cancelSeat("A1");
        System.out.println("Consistency Check after cancelling A1: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));

        System.out.println("Search A1 after cancel: " + manager.searchBooking("A1"));

        // 7. Cancel Unbooked Seat
        System.out.println("\n7. Testing Cancel Unbooked Seat:");
        boolean cancelOpenResult = manager.cancelSeat("C5");
        System.out.println("Cancel unbooked result: " + (!cancelOpenResult ? "PASSED (Prevented)" : "FAILED"));
        System.out.println("Consistency Check after invalid cancel: " + (manager.verifyStateConsistency() ? "PASSED" : "FAILED"));

        // 8. Display Seat Map
        System.out.println("\n8. Rendering updated seat map:");
        manager.displaySeatMap();

        System.out.println("--- Phase 2 Test Run Completed Successfully ---");
    }
}
