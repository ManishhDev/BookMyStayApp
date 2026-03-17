/**
 * Use Case 2: Basic Room Types and Static Availability.
 * Demonstrates abstraction, inheritance, polymorphism, and static state variables.
 *
 * @author Manish
 * @version 2.0
 */
public class UseCase2RoomInitialization {

    /**
     * Abstract base class representing common room properties.
     */
    static abstract class Room {
        private final String roomType;
        private final int beds;
        private final int sizeInSqFt;
        private final double pricePerNight;

        protected Room(String roomType, int beds, int sizeInSqFt, double pricePerNight) {
            this.roomType = roomType;
            this.beds = beds;
            this.sizeInSqFt = sizeInSqFt;
            this.pricePerNight = pricePerNight;
        }

        public String getRoomType() {
            return roomType;
        }

        public int getBeds() {
            return beds;
        }

        public int getSizeInSqFt() {
            return sizeInSqFt;
        }

        public double getPricePerNight() {
            return pricePerNight;
        }

        public void displayRoomDetails(int availableCount) {
            System.out.println("Room Type      : " + getRoomType());
            System.out.println("Beds           : " + getBeds());
            System.out.println("Size (sq ft)   : " + getSizeInSqFt());
            System.out.println("Price/Night    : $" + getPricePerNight());
            System.out.println("Availability   : " + availableCount);
            System.out.println("----------------------------------------");
        }
    }

    static class SingleRoom extends Room {
        public SingleRoom() {
            super("Single Room", 1, 180, 89.99);
        }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() {
            super("Double Room", 2, 280, 149.99);
        }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() {
            super("Suite Room", 3, 450, 299.99);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      BOOK MY STAY APP - ROOM INITIALIZATION      ");
        System.out.println("                 Version: 2.0                     ");
        System.out.println("==================================================");

        // Domain objects (what a room is)
        Room single = new SingleRoom();
        Room doubleRoom = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Static availability variables (current state)
        int singleAvailability = 12;
        int doubleAvailability = 8;
        int suiteAvailability = 3;

        System.out.println("\nAvailable Room Types and Current Availability");
        System.out.println("----------------------------------------");

        single.displayRoomDetails(singleAvailability);
        doubleRoom.displayRoomDetails(doubleAvailability);
        suite.displayRoomDetails(suiteAvailability);

        System.out.println("Application terminated successfully.");
    }
}
