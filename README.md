# AtomProject

 # 🎬 Atom-Project: Theater Seat Booking System

**Author:** Abhinav Raj 
**Institution:** Sapthagiri NPS. University  
**Program:** B.Tech in Computer Science and Engineering  

---

## 📖 Given Problem Statement
The objective is to design and implement a robust back-end booking manager and interactive command-line interface (CLI) for a movie theater. The system must process reservations using alphanumeric seat identifiers (e.g., "A1", "B3"), accurately map them to a physical 2D layout, assign customers to specific seats, and calculate live theater occupancy. The solution must survive hostile quality assurance (QA) stress tests by preventing double-bookings, rejecting invalid boundaries, and maintaining strict data synchronization across multiple data structures without relying on a traditional database.

## 🎯 Objectives
* **Custom Data Structures:** Build a dedicated 2D matrix (`SeatMap2D`) to manage spatial coordinates and visual grid rendering.
* **State Synchronization:** Maintain perfect consistency across a physical grid, a fast-lookup set for unique constraints, and a relational map for customer data.
* **Input Sanitization:** Handle dynamic and invalid user inputs gracefully, including string normalization and bounding limits.
* **Interactive UI:** Provide a continuous, console-based application loop for real-time ticket management.
* **Automated Testing:** Validate system integrity using an isolated, 12-point automated stress test suite.

## 🧠 DSA Concepts Used
This project bypasses traditional databases in favor of highly optimized in-memory Data Structures and Algorithms (DSA):

1. **2D Array / Matrix (`SeatMap2D`)**
   * Represents the physical layout of the theater grid.
   * Handles spatial coordinates, boundary validation, and visual rendering.
   * **Space Complexity:** $O(R \times C)$ where $R$ is rows and $C$ is columns.
2. **HashSet (`HashSet<String>`)**
   * Stores booked seat IDs to ensure uniqueness and prevent double-booking.
   * **Time Complexity:** $O(1)$ average for availability checks.
3. **HashMap (`HashMap<String, String>`)**
   * Maps specific seat IDs to customer names for relationship management.
   * **Time Complexity:** $O(1)$ average for data retrieval.

## ⚙️ Algorithm / Logic
1. **ID Parsing & Validation:** Receive alphanumeric string (e.g., "B3"). Trim, convert to uppercase, and validate using Regex. Extract row/col indices and verify they fall within matrix bounds.
2. **Booking Flow:** Query the `HashSet` in $O(1)$ time. If the seat exists, reject as a Double Booking. Otherwise, update the 2D matrix to `BOOKED` ('X'), and add the data to both the `HashSet` and `HashMap`.
3. **Cancellation Flow:** Query the `HashSet`. If the seat doesn't exist, reject. Otherwise, update the matrix to `AVAILABLE` ('O') and remove the entries from the Collections.

## 💻 Implementation 

### Project Architecture
* `MovieBookingApp.java` - Main driver class and interactive console UI.
* `BookingManager.java` - Core business logic controller synchronizing the data structures.
* `SeatMap2D.java` - Custom spatial matrix for grid management.
* `SystemTestSuite.java` - Automated unit and stress testing suite.

### How to Run Locally
1. Clone the repository:
   ```bash
   git clone [https://github.com/Abhinav-09-2004/Atom-Project.git](https://github.com/Abhinav-09-2004/Atom-Project.git)
   cd Atom-Project
