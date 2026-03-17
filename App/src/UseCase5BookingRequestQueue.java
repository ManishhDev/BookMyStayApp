import java.util.LinkedList;
import java.util.Queue;

/**
 * Use Case 5: Booking Request (First-Come-First-Served).
 * Demonstrates fair request intake using a FIFO queue.
 *
 * @author Manish
 * @version 5.0
 */
public class UseCase5BookingRequestQueue {

    /**
     * Represents a guest booking intent. No allocation is done at this stage.
     */
    static class Reservation {
        private final String guestName;
        private final String roomType;
        private final int nights;

        public Reservation(String guestName, String roomType, int nights) {
            this.guestName = guestName;
            this.roomType = roomType;
            this.nights = nights;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }

        public int getNights() {
            return nights;
        }

        @Override
        public String toString() {
            return "Reservation{guest='" + guestName + "', roomType='" + roomType
                + "', nights=" + nights + "}";
        }
    }

    /**
     * Intake queue for booking requests. Requests remain pending for later allocation.
     */
    static class BookingRequestQueue {
        private final Queue<Reservation> requestQueue = new LinkedList<>();

        public void submitRequest(Reservation reservation) {
            requestQueue.offer(reservation);
            System.out.println("Request received: " + reservation);
        }

        public void displayQueuedRequests() {
            System.out.println("\nQueued Booking Requests (FIFO Order)");
            System.out.println("----------------------------------------");
            if (requestQueue.isEmpty()) {
                System.out.println("No booking requests in queue.");
                return;
            }

            int position = 1;
            for (Reservation reservation : requestQueue) {
                System.out.println(position + ". " + reservation);
                position++;
            }
            System.out.println("----------------------------------------");
        }

        public int size() {
            return requestQueue.size();
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   BOOK MY STAY APP - BOOKING REQUEST QUEUE       ");
        System.out.println("                 Version: 5.0                     ");
        System.out.println("==================================================");

        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        System.out.println("\nGuest requests are arriving...");
        bookingQueue.submitRequest(new Reservation("Aarav", "Single Room", 2));
        bookingQueue.submitRequest(new Reservation("Meera", "Double Room", 3));
        bookingQueue.submitRequest(new Reservation("Rohan", "Suite Room", 1));
        bookingQueue.submitRequest(new Reservation("Isha", "Single Room", 4));

        bookingQueue.displayQueuedRequests();

        System.out.println("Total pending requests: " + bookingQueue.size());
        System.out.println("Inventory not modified at request intake stage.");
        System.out.println("Application terminated successfully.");
    }
}
