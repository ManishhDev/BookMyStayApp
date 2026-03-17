import java.util.HashMap;
import java.util.Map;

/**
 * Use Case 3: Centralized Room Inventory Management.
 * Demonstrates single-source room availability using HashMap.
 *
 * @author Manish
 * @version 3.0
 */
public class UseCase3InventorySetup {

    /**
     * Inventory component responsible only for room availability state.
     */
    static class RoomInventory {
        private final HashMap<String, Integer> availabilityMap;

        public RoomInventory() {
            availabilityMap = new HashMap<>();
            availabilityMap.put("Single Room", 12);
            availabilityMap.put("Double Room", 8);
            availabilityMap.put("Suite Room", 3);
        }

        public int getAvailability(String roomType) {
            return availabilityMap.getOrDefault(roomType, 0);
        }

        public void updateAvailability(String roomType, int newCount) {
            if (newCount < 0) {
                System.out.println("Invalid availability for " + roomType + ": cannot be negative.");
                return;
            }
            availabilityMap.put(roomType, newCount);
        }

        public void displayInventory() {
            System.out.println("Current Centralized Inventory");
            System.out.println("----------------------------------------");
            for (Map.Entry<String, Integer> entry : availabilityMap.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
            System.out.println("----------------------------------------");
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   BOOK MY STAY APP - INVENTORY MANAGEMENT        ");
        System.out.println("                 Version: 3.0                     ");
        System.out.println("==================================================");

        // Initialize centralized inventory (single source of truth).
        RoomInventory inventory = new RoomInventory();

        System.out.println("\nInitial Room Inventory:");
        inventory.displayInventory();

        System.out.println("\nChecking availability (O(1) lookup):");
        System.out.println("Single Room availability: " + inventory.getAvailability("Single Room"));
        System.out.println("Suite Room availability : " + inventory.getAvailability("Suite Room"));

        System.out.println("\nUpdating room counts through controlled methods:");
        inventory.updateAvailability("Single Room", 10);
        inventory.updateAvailability("Suite Room", 2);

        System.out.println("\nInventory after updates:");
        inventory.displayInventory();

        System.out.println("Application terminated successfully.");
    }
}
