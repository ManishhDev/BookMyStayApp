import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Use Case 6: Reservation Confirmation and Room Allocation.
 * Demonstrates FIFO booking confirmation with unique room assignment and immediate inventory sync.
 *
 * @author Manish
 * @version 6.0
 */
public class UseCase6RoomAllocationService {

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
    }

    static class RoomInventory {
        private final HashMap<String, Integer> availabilityMap = new HashMap<>();

        public RoomInventory() {
            availabilityMap.put("Single Room", 2);
            availabilityMap.put("Double Room", 1);
            availabilityMap.put("Suite Room", 1);
        }

        public int getAvailability(String roomType) {
            return availabilityMap.getOrDefault(roomType, 0);
        }

        public boolean decrement(String roomType) {
            int current = getAvailability(roomType);
            if (current <= 0) {
                return false;
            }
            availabilityMap.put(roomType, current - 1);
            return true;
        }

        public void displayInventory() {
            System.out.println("Inventory State");
            System.out.println("----------------------------------------");
            for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
            System.out.println("----------------------------------------");
        }
    }

    static class BookingService {
        private final RoomInventory inventory;
        private final Queue<Reservation> requestQueue;
        private final Set<String> allocatedRoomIds = new HashSet<>();
        private final HashMap<String, Set<String>> allocatedByRoomType = new HashMap<>();
        private final HashMap<String, Integer> roomTypeCounters = new HashMap<>();

        public BookingService(RoomInventory inventory, Queue<Reservation> requestQueue) {
            this.inventory = inventory;
            this.requestQueue = requestQueue;
        }

        public void processAllRequests() {
            System.out.println("\nProcessing booking queue in FIFO order...");
            while (!requestQueue.isEmpty()) {
                Reservation reservation = requestQueue.poll();
                confirmReservation(reservation);
            }
        }

        private void confirmReservation(Reservation reservation) {
            String roomType = reservation.getRoomType();

            System.out.println("\nDequeued request: " + reservation.getGuestName()
                + " requested " + roomType + " for " + reservation.getNights() + " night(s)");

            if (inventory.getAvailability(roomType) <= 0) {
                System.out.println("Status: REJECTED (No availability for " + roomType + ")");
                return;
            }

            String roomId = generateUniqueRoomId(roomType);
            allocatedRoomIds.add(roomId);
            allocatedByRoomType.computeIfAbsent(roomType, key -> new HashSet<>()).add(roomId);

            // Allocation and inventory update happen together as one logical unit.
            inventory.decrement(roomType);

            System.out.println("Status: CONFIRMED");
            System.out.println("Assigned Room ID: " + roomId);
            System.out.println("Remaining " + roomType + " inventory: " + inventory.getAvailability(roomType));
        }

        private String generateUniqueRoomId(String roomType) {
            String prefix = roomType.substring(0, 1).toUpperCase();
            int next = roomTypeCounters.getOrDefault(roomType, 0) + 1;
            String roomId = prefix + String.format("-%03d", next);

            while (allocatedRoomIds.contains(roomId)) {
                next++;
                roomId = prefix + String.format("-%03d", next);
            }

            roomTypeCounters.put(roomType, next);
            return roomId;
        }

        public void displayAllocatedRooms() {
            System.out.println("\nAllocated Room IDs By Type");
            System.out.println("----------------------------------------");
            for (Map.Entry<String, Set<String>> entry : allocatedByRoomType.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
            System.out.println("Total unique assigned rooms: " + allocatedRoomIds.size());
            System.out.println("----------------------------------------");
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   BOOK MY STAY APP - ROOM ALLOCATION SERVICE     ");
        System.out.println("                 Version: 6.0                     ");
        System.out.println("==================================================");

        RoomInventory inventory = new RoomInventory();

        Queue<Reservation> bookingQueue = new LinkedList<>();
        bookingQueue.offer(new Reservation("Aarav", "Single Room", 2));
        bookingQueue.offer(new Reservation("Meera", "Double Room", 1));
        bookingQueue.offer(new Reservation("Rohan", "Single Room", 3));
        bookingQueue.offer(new Reservation("Isha", "Suite Room", 2));
        bookingQueue.offer(new Reservation("Kabir", "Suite Room", 1));

        System.out.println("\nInitial queue size: " + bookingQueue.size());
        inventory.displayInventory();

        BookingService bookingService = new BookingService(inventory, bookingQueue);
        bookingService.processAllRequests();

        bookingService.displayAllocatedRooms();
        inventory.displayInventory();

        System.out.println("Application terminated successfully.");
    }
}
