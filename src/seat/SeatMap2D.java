package seat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Custom 2D Data Structure representing a Movie Theater Seat Layout.
 *
 * Manages a 2D grid array where:
 *   'O' = Available seat
 *   'X' = Booked seat
 *
 * Provides methods for:
 * - Seat ID to 2D coordinate conversion
 * - Seat state checking
 * - Booking
 * - Cancellation
 * - Seat map rendering
 */
public class SeatMap2D {

    // Seat state constants
    public static final char AVAILABLE = 'O';
    public static final char BOOKED = 'X';

    private final int rows;
    private final int cols;
    private final char[][] grid;

    /**
     * Creates a theater seat map.
     *
     * @param rows number of seating rows
     * @param cols number of seating columns
     */
    public SeatMap2D(int rows, int cols) {

        if (rows <= 0 || rows > 26) {
            throw new IllegalArgumentException(
                    "Rows must be between 1 and 26 (A-Z)."
            );
        }

        if (cols <= 0) {
            throw new IllegalArgumentException(
                    "Columns must be greater than 0."
            );
        }

        this.rows = rows;
        this.cols = cols;
        this.grid = new char[rows][cols];

        initializeGrid();
    }

    /**
     * Initializes every seat as AVAILABLE ('O').
     *
     * Time Complexity: O(rows * cols)
     */
    private void initializeGrid() {

        for (int r = 0; r < rows; r++) {

            for (int c = 0; c < cols; c++) {

                grid[r][c] = AVAILABLE;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Returns total number of seats.
     *
     * Time Complexity: O(1)
     */
    public int getTotalSeats() {
        return rows * cols;
    }

    /**
     * Checks whether a row and column are inside the matrix.
     *
     * Time Complexity: O(1)
     */
    public boolean isValidBounds(int row, int col) {

        return row >= 0
                && row < rows
                && col >= 0
                && col < cols;
    }

    /**
     * Converts a seat ID such as "B3" into
     * 0-based matrix coordinates.
     *
     * Example:
     * B3 -> {1, 2}
     *
     * @return {row, col} if valid, otherwise null
     *
     * Time Complexity: O(L), where L is the length of seatId.
     */
    public int[] parseSeatId(String seatId) {

        if (seatId == null) {
            return null;
        }

        String cleaned = seatId.trim().toUpperCase();

        /*
         * Valid format:
         * A1
         * B3
         * E8
         *
         * Invalid:
         * 1A
         * ABC
         * A
         * A0
         * A-1
         */
        if (!cleaned.matches("^[A-Z][1-9][0-9]*$")) {
            return null;
        }

        char rowChar = cleaned.charAt(0);

        int row = rowChar - 'A';

        int col;

        try {
            col = Integer.parseInt(cleaned.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        if (!isValidBounds(row, col)) {
            return null;
        }

        return new int[]{row, col};
    }

    /**
     * Checks whether a particular seat is booked.
     *
     * Time Complexity: O(1)
     */
    public boolean isBooked(int row, int col) {

        if (!isValidBounds(row, col)) {
            return false;
        }

        return grid[row][col] == BOOKED;
    }

    /**
     * Books a seat.
     *
     * Time Complexity: O(1)
     */
    public boolean bookSeat(int row, int col) {

        if (!isValidBounds(row, col)) {
            return false;
        }

        // Prevent double booking at matrix level
        if (grid[row][col] == BOOKED) {
            return false;
        }

        grid[row][col] = BOOKED;

        return true;
    }

    /**
     * Cancels a booked seat.
     *
     * Time Complexity: O(1)
     */
    public boolean cancelSeat(int row, int col) {

        if (!isValidBounds(row, col)) {
            return false;
        }

        // Cannot cancel an already available seat
        if (grid[row][col] == AVAILABLE) {
            return false;
        }

        grid[row][col] = AVAILABLE;

        return true;
    }

    /**
     * Counts all booked seats in the matrix using Java Stream API.
     *
     * Time Complexity: O(rows * cols)
     */
    public int getBookedCount() {
        return (int) IntStream.range(0, rows)
                .flatMap(r -> IntStream.range(0, cols).filter(c -> grid[r][c] == BOOKED))
                .count();
    }

    /**
     * Counts all available seats in the matrix using Java Stream API.
     *
     * Time Complexity: O(rows * cols)
     */
    public int getAvailableCount() {
        return (int) IntStream.range(0, rows)
                .flatMap(r -> IntStream.range(0, cols).filter(c -> grid[r][c] == AVAILABLE))
                .count();
    }

    /**
     * Returns a List of formatted seat IDs (e.g. "A1", "B2") that are currently available.
     * Uses Java Stream API.
     */
    public List<String> getAvailableSeatsList() {
        return IntStream.range(0, rows)
                .boxed()
                .flatMap(r -> IntStream.range(0, cols)
                        .filter(c -> grid[r][c] == AVAILABLE)
                        .mapToObj(c -> "" + (char) ('A' + r) + (c + 1)))
                .collect(Collectors.toList());
    }

    /**
     * Returns a List of formatted seat IDs (e.g. "A1", "B2") that are currently booked.
     * Uses Java Stream API.
     */
    public List<String> getBookedSeatsList() {
        return IntStream.range(0, rows)
                .boxed()
                .flatMap(r -> IntStream.range(0, cols)
                        .filter(c -> grid[r][c] == BOOKED)
                        .mapToObj(c -> "" + (char) ('A' + r) + (c + 1)))
                .collect(Collectors.toList());
    }

    /**
     * Calculates theater occupancy percentage.
     *
     * Time Complexity: O(rows * cols)
     */
    public double getOccupancyPercentage() {

        int totalSeats = getTotalSeats();

        if (totalSeats == 0) {
            return 0.0;
        }

        int bookedSeats = getBookedCount();

        return (bookedSeats * 100.0) / totalSeats;
    }

    /**
     * Prints the complete theater seat map.
     *
     * Time Complexity: O(rows * cols)
     */
    public void printMap() {

        System.out.println(
                "\n================================================="
        );

        System.out.println(
                "                 [ S C R E E N ]"
        );

        System.out.println(
                "   -------------------------------------------"
        );

        // Column numbers
        System.out.print("    ");

        for (int c = 1; c <= cols; c++) {
            System.out.printf("%-4d", c);
        }

        System.out.println();

        // Seat rows
        for (int r = 0; r < rows; r++) {

            char rowLabel = (char) ('A' + r);

            System.out.print(rowLabel + "   ");

            for (int c = 0; c < cols; c++) {

                System.out.print("[" + grid[r][c] + "] ");
            }

            System.out.println();
        }

        // Calculate once instead of scanning twice
        int bookedCount = getBookedCount();

        double occupancy =
                (bookedCount * 100.0) / getTotalSeats();

        System.out.println(
                "\nLegend: ["
                        + AVAILABLE
                        + "] Available   ["
                        + BOOKED
                        + "] Booked"
        );

        System.out.printf(
                "Occupancy: %.2f%% (%d / %d seats booked)%n",
                occupancy,
                bookedCount,
                getTotalSeats()
        );

        System.out.println(
                "=================================================\n"
        );
    }
}