import java.util.Map;
import java.util.Scanner;

/**
 * Main Console Application for Movie Ticket Booking System (Java + DSA Assessment).
 *
 * Provides an interactive menu interface to interact with BookingManager.
 */
public class MovieBookingApp {

    private static final int THEATER_ROWS = 5; // Rows A through E
    private static final int THEATER_COLS = 8; // Columns 1 through 8

    private final BookingManager bookingManager;
    private final Scanner scanner;

    public MovieBookingApp() {
        this.bookingManager = new BookingManager(THEATER_ROWS, THEATER_COLS);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Entry point of the application.
     */
    public static void main(String[] args) {
        MovieBookingApp app = new MovieBookingApp();
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {

        boolean running = true;

        printWelcomeMessage();

        while (running) {

            displayMenu();

            System.out.print("👉 Select an option (1-7): ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                printError("Input cannot be empty.");
                continue;
            }

            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("Invalid input! Please enter a number between 1 and 7.");
                continue;
            }

            switch (choice) {

                case 1 -> handleViewSeatLayout();

                case 2 -> handleBookSeat();

                case 3 -> handleCancelBooking();

                case 4 -> handleSearchBooking();

                case 5 -> handleViewAllBookings();

                case 6 -> handleViewOccupancyStats();

                case 7 -> {
                    printHeader("THANK YOU FOR USING CINEMA STAR SYSTEM!");
                    System.out.println("   Have a great movie experience! Goodbye. 🍿\n");
                    running = false;
                }

                default ->
                        printError("Choice out of range! Please choose an option from 1 to 7.");
            }
        }

        scanner.close();
    }

    /**
     * Displays the welcome message.
     */
    private void printWelcomeMessage() {

        System.out.println("\n" + "=".repeat(55));
        System.out.println(" 🎬 WELCOME TO CINEMA STAR TICKET BOOKING SYSTEM 🎬");
        System.out.println("=".repeat(55));
    }

    /**
     * Displays the main menu.
     */
    private void displayMenu() {

        System.out.println("\n" + "─".repeat(55));
        System.out.println("                    MAIN MENU");
        System.out.println("─".repeat(55));

        System.out.println("  1. 💺 View Seat Layout & Map");
        System.out.println("  2. 🎟️  Book a Seat");
        System.out.println("  3. ❌ Cancel a Seat Booking");
        System.out.println("  4. 🔍 Search Booking by Seat ID");
        System.out.println("  5. 📋 View All Active Bookings");
        System.out.println("  6. 📊 View Occupancy Statistics");
        System.out.println("  7. 🚪 Exit Application");

        System.out.println("─".repeat(55));
    }

    /**
     * Displays the current seat layout.
     */
    private void handleViewSeatLayout() {

        printHeader("THEATER SEAT LAYOUT");

        bookingManager.displaySeatMap();
    }

    /**
     * Handles seat booking using custom exception handling.
     */
    private void handleBookSeat() {

        printHeader("BOOK A MOVIE SEAT");

        System.out.print("Enter Seat ID (e.g., A1, B3): ");
        String seatId = scanner.nextLine();

        System.out.print("Enter Customer Name: ");
        String customerName = scanner.nextLine();

        System.out.println("\nProcessing booking request...");

        try {
            bookingManager.bookSeatWithException(seatId, customerName);

            String normalizedSeatId = normalizeSeatId(seatId);
            String normalizedCustomerName = customerName.trim();

            printSuccessSummary(
                    "BOOKING CONFIRMED",
                    Map.of(
                            "Seat ID", normalizedSeatId,
                            "Customer Name", normalizedCustomerName,
                            "Theater Screen", "Screen 1 (Audi A)",
                            "Status", "CONFIRMED [X]"
                    )
            );
        } catch (BookingManager.InvalidCustomerException e) {
            printError(e.getMessage());
        } catch (BookingManager.InvalidSeatException e) {
            printError(e.getMessage());
        } catch (BookingManager.SeatAlreadyBookedException e) {
            printError(e.getMessage());
        }
    }

    /**
     * Handles cancellation of an existing booking using custom exception handling.
     */
    private void handleCancelBooking() {

        printHeader("CANCEL SEAT BOOKING");

        System.out.print("Enter Seat ID to Cancel (e.g., A1, B3): ");
        String seatId = scanner.nextLine();

        String normalizedId = normalizeSeatId(seatId);

        /*
         * Get the customer before cancellation because the mapping
         * is removed by BookingManager during cancellation.
         */
        String customerName =
                bookingManager.getSeatCustomerMap().get(normalizedId);

        System.out.println("\nProcessing cancellation request...");

        try {
            bookingManager.cancelSeatWithException(seatId);

            printSuccessSummary(
                    "CANCELLATION CONFIRMED",
                    Map.of(
                            "Seat ID", normalizedId,
                            "Previous Customer",
                            customerName != null ? customerName : "N/A",
                            "Status", "RELEASED TO AVAILABLE [O]"
                    )
            );
        } catch (BookingManager.InvalidSeatException e) {
            printError(e.getMessage());
        } catch (BookingManager.SeatNotBookedException e) {
            printError(e.getMessage());
        }
    }

    /**
     * Searches for a booking using the seat ID.
     */
    private void handleSearchBooking() {

        printHeader("SEARCH BOOKING DETAILS");

        System.out.print("Enter Seat ID to Search (e.g., A1, B3): ");
        String seatId = scanner.nextLine();

        System.out.println("\nSearch Result:");

        String result = bookingManager.searchBooking(seatId);

        System.out.println("ℹ️  " + result);
    }

    /**
     * Displays all currently active bookings.
     */
    private void handleViewAllBookings() {

        printHeader("ACTIVE BOOKINGS MANIFEST");

        Map<String, String> bookings =
                bookingManager.getSeatCustomerMap();

        if (bookings.isEmpty()) {

            System.out.println(
                    "ℹ️  No active bookings found. " +
                    "All seats are currently AVAILABLE."
            );

            return;
        }

        System.out.println(
                "┌─────────────┬─────────────────────────────────┐"
        );

        System.out.println(
                "│ Seat ID     │ Customer Name                   │"
        );

        System.out.println(
                "├─────────────┼─────────────────────────────────┤"
        );

        /*
         * HashMap does not guarantee ordering.
         * Sorting the keys gives a clean A1, A2, A3... presentation.
         */
        bookings.keySet()
                .stream()
                .sorted()
                .forEach(seatId -> {

                    String customer = bookings.get(seatId);

                    String displayCustomer =
                            customer.length() > 30
                                    ? customer.substring(0, 27) + "..."
                                    : customer;

                    System.out.printf(
                            "│ %-11s │ %-31s │%n",
                            seatId,
                            displayCustomer
                    );
                });

        System.out.println(
                "└─────────────┴─────────────────────────────────┘"
        );

        System.out.println(
                "Total Booked Seats: " + bookings.size()
        );
    }

    /**
     * Displays occupancy and capacity statistics.
     */
    private void handleViewOccupancyStats() {

        printHeader("OCCUPANCY & CAPACITY STATISTICS");

        int totalSeats =
                bookingManager.getSeatMap().getTotalSeats();

        int bookedCount =
                bookingManager.getBookedSeatCount();

        int availableCount =
                totalSeats - bookedCount;

        double occupancyPercentage =
                bookingManager.calculateOccupancyPercentage();

        System.out.println(
                "  • Theater Capacity  : "
                        + totalSeats
                        + " seats"
        );

        System.out.println(
                "  • Booked Seats      : "
                        + bookedCount
                        + " seats [X]"
        );

        System.out.println(
                "  • Available Seats   : "
                        + availableCount
                        + " seats [O]"
        );

        System.out.printf(
                "  • Occupancy Rate    : %.2f%%%n",
                occupancyPercentage
        );

        displayOccupancyProgressBar(occupancyPercentage);
    }

    /**
     * Displays a visual occupancy progress bar.
     */
    private void displayOccupancyProgressBar(
            double occupancyPercentage) {

        int barWidth = 30;

        int filledWidth =
                (int) Math.round(
                        (occupancyPercentage / 100.0)
                                * barWidth
                );

        StringBuilder bar =
                new StringBuilder("[");

        for (int i = 0; i < barWidth; i++) {

            if (i < filledWidth) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }

        bar.append("]");

        System.out.println(
                "  • Visual Capacity   : "
                        + bar
                        + " "
                        + String.format(
                                "%.1f%%",
                                occupancyPercentage
                        )
        );
    }

    /**
     * Normalizes a seat ID for display purposes.
     */
    private String normalizeSeatId(String seatId) {

        if (seatId == null) {
            return "";
        }

        return seatId.trim().toUpperCase();
    }

    /**
     * Prints a formatted section header.
     */
    private void printHeader(String title) {

        System.out.println(
                "\n================================================="
        );

        System.out.println("  " + title);

        System.out.println(
                "================================================="
        );
    }

    /**
     * Prints an error message.
     */
    private void printError(String message) {

        System.out.println(
                "\n❌ [ERROR]: " + message
        );
    }

    /**
     * Prints a formatted success summary.
     */
    private void printSuccessSummary(
            String title,
            Map<String, String> details) {

        System.out.println(
                "\n┌───────────────────────────────────────────────┐"
        );

        System.out.printf(
                "│ 🟢 %-42s │%n",
                title
        );

        System.out.println(
                "├───────────────────────────────────────────────┤"
        );

        for (Map.Entry<String, String> entry :
                details.entrySet()) {

            String value = entry.getValue();

            String displayValue =
                    value.length() > 25
                            ? value.substring(0, 22) + "..."
                            : value;

            System.out.printf(
                    "│ %-16s : %-25s │%n",
                    entry.getKey(),
                    displayValue
            );
        }

        System.out.println(
                "└───────────────────────────────────────────────┘"
        );
    }
}
