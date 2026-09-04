package booking;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import seat.SeatMap2D;

/**
 * BookingManager handles movie ticket booking business logic.
 * 
 * Coordinates three data structures to ensure state consistency:
 *   1. Custom SeatMap2D (physical layout matrix grid)
 *   2. HashSet<String> bookedSeats (O(1) fast uniqueness and availability lookup)
 *   3. HashMap<String, String> seatCustomerMap (O(1) seat ID -> customer name relation)
 */
public class BookingManager {

    // =========================================================================
    // CUSTOM EXCEPTIONS (Defined inside file without creating extra files)
    // =========================================================================

    /**
     * Base custom checked exception for the Cinema Booking System.
     */
    public static class MovieBookingException extends Exception {
        public MovieBookingException(String message) {
            super(message);
        }
        public MovieBookingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom Exception 1: Thrown when a seat ID is malformed or out of theater grid boundaries.
     */
    public static class InvalidSeatException extends MovieBookingException {
        public InvalidSeatException(String message) {
            super(message);
        }
    }

    /**
     * Custom Exception 2: Thrown when a booking attempt is made on an already booked seat.
     */
    public static class SeatAlreadyBookedException extends MovieBookingException {
        public SeatAlreadyBookedException(String message) {
            super(message);
        }
    }

    /**
     * Custom Exception 3: Thrown when attempting to cancel or reference an unbooked seat.
     */
    public static class SeatNotBookedException extends MovieBookingException {
        public SeatNotBookedException(String message) {
            super(message);
        }
    }

    /**
     * Custom Exception 4: Thrown when customer name is null, empty, or whitespace.
     */
    public static class InvalidCustomerException extends MovieBookingException {
        public InvalidCustomerException(String message) {
            super(message);
        }
    }

    /**
     * Custom Exception 5: Thrown when theater rows/cols dimensions are out of valid range.
     */
    public static class InvalidTheaterDimensionsException extends MovieBookingException {
        public InvalidTheaterDimensionsException(String message) {
            super(message);
        }
    }

    // =========================================================================
    // CORE FIELDS & CONSTRUCTOR
    // =========================================================================

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
        if (rows <= 0 || rows > 26 || cols <= 0) {
            throw new IllegalArgumentException("Invalid theater dimensions: rows=" + rows + ", cols=" + cols);
        }
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
    public String normalizeSeatId(String seatId) {
        return seatId == null ? "" : seatId.trim().toUpperCase();
    }

    // =========================================================================
    // EXCEPTION-THROWING BUSINESS METHODS
    // =========================================================================

    /**
     * Books a seat and throws explicit custom exceptions on any failure.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "B3")
     * @param customerName Name of booking customer
     * @throws InvalidCustomerException if name is null, empty, or whitespace
     * @throws InvalidSeatException if seat ID format is invalid or out of bounds
     * @throws SeatAlreadyBookedException if seat is already booked
     */
    public void bookSeatWithException(String seatId, String customerName)
            throws InvalidCustomerException, InvalidSeatException, SeatAlreadyBookedException {

        // 1. Validate customer name
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new InvalidCustomerException("Customer name cannot be empty or whitespace.");
        }

        // 2. Normalize seat ID
        String cleanSeatId = normalizeSeatId(seatId);

        // 3. Parse and validate seat ID format & grid boundaries
        int[] coords = seatMap.parseSeatId(cleanSeatId);
        if (coords == null) {
            throw new InvalidSeatException("Seat ID '" + seatId + "' is invalid or out of theater bounds.");
        }

        int row = coords[0];
        int col = coords[1];

        // 4. Prevent double booking using HashSet O(1) lookup
        if (bookedSeats.contains(cleanSeatId)) {
            throw new SeatAlreadyBookedException("Seat '" + cleanSeatId + "' is already booked by " + seatCustomerMap.get(cleanSeatId) + "!");
        }

        // 5. Update custom 2D matrix
        boolean matrixSuccess = seatMap.bookSeat(row, col);
        if (!matrixSuccess) {
            throw new SeatAlreadyBookedException("Matrix cell conflict: Seat '" + cleanSeatId + "' is unavailable.");
        }

        // 6. Update HashSet and HashMap to keep state synchronized
        String cleanName = customerName.trim();
        bookedSeats.add(cleanSeatId);
        seatCustomerMap.put(cleanSeatId, cleanName);
    }

    /**
     * Cancels an existing seat booking and throws explicit custom exceptions on failure.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "B3")
     * @throws InvalidSeatException if seat ID is invalid or out of bounds
     * @throws SeatNotBookedException if seat is not currently booked
     */
    public void cancelSeatWithException(String seatId)
            throws InvalidSeatException, SeatNotBookedException {

        // 1. Normalize seat ID
        String cleanSeatId = normalizeSeatId(seatId);

        // 2. Parse and validate seat ID format & grid boundaries
        int[] coords = seatMap.parseSeatId(cleanSeatId);
        if (coords == null) {
            throw new InvalidSeatException("Seat ID '" + seatId + "' is invalid or out of theater bounds.");
        }

        int row = coords[0];
        int col = coords[1];

        // 3. Check if seat is currently booked using HashSet O(1) lookup
        if (!bookedSeats.contains(cleanSeatId)) {
            throw new SeatNotBookedException("Seat '" + cleanSeatId + "' is not currently booked!");
        }

        // 4. Update custom 2D matrix
        boolean matrixSuccess = seatMap.cancelSeat(row, col);
        if (!matrixSuccess) {
            throw new SeatNotBookedException("Matrix cell conflict: Seat '" + cleanSeatId + "' is not marked booked.");
        }

        // 5. Update HashSet and HashMap to maintain consistency
        seatCustomerMap.remove(cleanSeatId);
        bookedSeats.remove(cleanSeatId);
    }

    // =========================================================================
    // STANDARD BOOKING & CANCELLATION METHODS (Backward-Compatible)
    // =========================================================================

    /**
     * Books a seat for a given customer.
     * Catches custom exceptions and logs informative messages.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "b3")
     * @param customerName Name of booking customer
     * @return true if booking succeeded, false if invalid seat ID, empty name, or double booking
     */
    public boolean bookSeat(String seatId, String customerName) {
        try {
            bookSeatWithException(seatId, customerName);
            String cleanSeatId = normalizeSeatId(seatId);
            String cleanName = customerName.trim();
            System.out.println("✅ Success: Seat " + cleanSeatId + " successfully booked for " + cleanName + ".");
            return true;
        } catch (InvalidCustomerException e) {
            System.out.println("❌ Booking Error: " + e.getMessage());
            return false;
        } catch (InvalidSeatException e) {
            System.out.println("❌ Booking Error: " + e.getMessage());
            return false;
        } catch (SeatAlreadyBookedException e) {
            System.out.println("❌ Double Booking Alert: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancels an existing seat booking.
     * Catches custom exceptions and logs informative messages.
     * 
     * @param seatId Alphanumeric seat ID (e.g., "A1", "B3")
     * @return true if cancellation succeeded, false if invalid seat or seat not booked
     */
    public boolean cancelSeat(String seatId) {
        String cleanSeatId = normalizeSeatId(seatId);
        String customerName = seatCustomerMap.get(cleanSeatId);
        try {
            cancelSeatWithException(seatId);
            System.out.println("✅ Success: Booking for seat " + cleanSeatId + " (" + customerName + ") has been cancelled.");
            return true;
        } catch (InvalidSeatException e) {
            System.out.println("❌ Cancellation Error: " + e.getMessage());
            return false;
        } catch (SeatNotBookedException e) {
            System.out.println("❌ Cancellation Error: " + e.getMessage());
            return false;
        }
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

    // =========================================================================
    // STREAM API METHODS
    // =========================================================================

    /**
     * Returns all booked seat IDs in natural alphabetical sort order using Java Stream API.
     */
    public List<String> getSortedBookedSeats() {
        return bookedSeats.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Filters and finds all seat bookings whose customer name matches the search query.
     * Uses Java Stream API.
     */
    public Map<String, String> searchBookingsByCustomer(String customerNameQuery) {
        if (customerNameQuery == null || customerNameQuery.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        String lowerQuery = customerNameQuery.trim().toLowerCase();
        return seatCustomerMap.entrySet().stream()
                .filter(entry -> entry.getValue().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Returns list of all currently available seat IDs using Java Stream API.
     */
    public List<String> getAllAvailableSeatIds() {
        return seatMap.getAvailableSeatsList();
    }

    /**
     * Calculates summary counts of seats booked per customer using Java Stream API.
     */
    public Map<String, Long> getCustomerBookingCounts() {
        return seatCustomerMap.values().stream()
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));
    }

    /**
     * Verifies internal consistency across SeatMap2D, HashSet, and HashMap using Java Stream API.
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

        // Verify every seat in HashSet is marked 'X' in Matrix and present in HashMap using Stream API
        return bookedSeats.stream().allMatch(seatId -> {
            if (!seatCustomerMap.containsKey(seatId)) {
                return false;
            }
            int[] coords = seatMap.parseSeatId(seatId);
            return coords != null && seatMap.isBooked(coords[0], coords[1]);
        });
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

        // 9. Stream API verification
        System.out.println("\n9. Testing Stream API Methods:");
        System.out.println("Sorted booked seats (Stream): " + manager.getSortedBookedSeats());
        System.out.println("Total available seats count (Stream): " + manager.getAllAvailableSeatIds().size());

        System.out.println("--- Phase 2 Test Run Completed Successfully ---");
    }
}
