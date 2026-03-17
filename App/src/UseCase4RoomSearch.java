import java.util.HashMap;
import java.util.Map;

/**
 * Use Case 4: Room Search and Availability Check.
 * Demonstrates read-only room search with clear separation from inventory updates.
 *
 * @author Manish
 * @version 4.0
 */
public class UseCase4RoomSearch {

    static abstract class Room {
        private final String roomType;
        private final int beds;
        private final int sizeInSqFt;
        private final double pricePerNight;
        private final String amenities;

        protected Room(String roomType, int beds, int sizeInSqFt, double pricePerNight, String amenities) {
            this.roomType = roomType;
            this.beds = beds;
            this.sizeInSqFt = sizeInSqFt;
            this.pricePerNight = pricePerNight;
            this.amenities = amenities;
        }

        public String getRoomType() {
            return roomType;
        }

        public void displayDetails(int availability) {
            System.out.println("Room Type      : " + roomType);
            System.out.println("Beds           : " + beds);
            System.out.println("Size (sq ft)   : " + sizeInSqFt);
            System.out.println("Price/Night    : $" + pricePerNight);
            System.out.println("Amenities      : " + amenities);
            System.out.println("Availability   : " + availability);
            System.out.println("----------------------------------------");
        }
    }

    static class SingleRoom extends Room {
        public SingleRoom() {
            super("Single Room", 1, 180, 89.99, "Free WiFi, Work Desk");
        }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() {
            super("Double Room", 2, 280, 149.99, "Free WiFi, Balcony");
        }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() {
            super("Suite Room", 3, 450, 299.99, "Living Area, Jacuzzi");
        }
    }

    static class RoomInventory {
        private final HashMap<String, Integer> availabilityMap;

        public RoomInventory() {
            availabilityMap = new HashMap<>();
            availabilityMap.put("Single Room", 10);
            availabilityMap.put("Double Room", 0);
            availabilityMap.put("Suite Room", 2);
        }

        public int getAvailability(String roomType) {
            return availabilityMap.getOrDefault(roomType, 0);
        }

        // Defensive copy supports read-only operations from external classes.
        public Map<String, Integer> getAvailabilitySnapshot() {
            return new HashMap<>(availabilityMap);
        }
    }

    static class SearchService {
        private final RoomInventory inventory;
        private final Map<String, Room> roomCatalog;

        public SearchService(RoomInventory inventory, Map<String, Room> roomCatalog) {
            this.inventory = inventory;
            this.roomCatalog = roomCatalog;
        }

        public void displayAvailableRooms() {
            System.out.println("Available Room Options");
            System.out.println("----------------------------------------");

            boolean found = false;
            for (Map.Entry<String, Room> entry : roomCatalog.entrySet()) {
                String roomType = entry.getKey();
                Room room = entry.getValue();
                int availableCount = inventory.getAvailability(roomType);

                if (availableCount > 0) {
                    room.displayDetails(availableCount);
                    found = true;
                }
            }

            if (!found) {
                System.out.println("No rooms are currently available.");
                System.out.println("----------------------------------------");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      BOOK MY STAY APP - ROOM SEARCH              ");
        System.out.println("                 Version: 4.0                     ");
        System.out.println("==================================================");

        RoomInventory inventory = new RoomInventory();

        Map<String, Room> roomCatalog = new HashMap<>();
        roomCatalog.put("Single Room", new SingleRoom());
        roomCatalog.put("Double Room", new DoubleRoom());
        roomCatalog.put("Suite Room", new SuiteRoom());

        Map<String, Integer> beforeSearch = inventory.getAvailabilitySnapshot();

        SearchService searchService = new SearchService(inventory, roomCatalog);
        System.out.println("\nGuest initiates room search...\n");
        searchService.displayAvailableRooms();

        Map<String, Integer> afterSearch = inventory.getAvailabilitySnapshot();

        System.out.println("Inventory state changed during search: " + !beforeSearch.equals(afterSearch));
        System.out.println("Application terminated successfully.");
    }
}
