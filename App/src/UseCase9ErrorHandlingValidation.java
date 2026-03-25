import java.util.*;

public class UseCase9ErrorHandlingValidation {

    // Custom exceptions
    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String msg) { super(msg); }
    }
    static class InvalidRoomTypeException extends InvalidBookingException {
        public InvalidRoomTypeException(String msg) { super(msg); }
    }
    static class InsufficientInventoryException extends InvalidBookingException {
        public InsufficientInventoryException(String msg) { super(msg); }
    }

    // Simple inventory store
    static class Inventory {
        private final Map<String, Integer> stock = new HashMap<>();

        public Inventory() {
            stock.put("Single", 5);
            stock.put("Double", 3);
            stock.put("Suite", 1);
        }

        public synchronized void reserve(String roomType, int count) throws InvalidRoomTypeException, InsufficientInventoryException {
            if (roomType == null || !stock.containsKey(roomType)) {
                throw new InvalidRoomTypeException("Unknown room type: " + roomType);
            }
            int available = stock.get(roomType);
            if (count <= 0) throw new IllegalArgumentException("Requested room count must be positive");
            if (available < count) {
                throw new InsufficientInventoryException(String.format("Not enough '%s' rooms available (requested=%d, available=%d)", roomType, count, available));
            }
            stock.put(roomType, available - count);
        }

        public synchronized int available(String roomType) {
            return stock.getOrDefault(roomType, 0);
        }

        public Map<String,Integer> snapshot() {
            return Collections.unmodifiableMap(new HashMap<>(stock));
        }
    }

    // Validator
    static class BookingValidator {
        public static void validateInput(String guestName, String roomType, int count) throws InvalidBookingException {
            if (guestName == null || guestName.trim().isEmpty()) throw new InvalidBookingException("Guest name is required");
            if (roomType == null || roomType.trim().isEmpty()) throw new InvalidBookingException("Room type is required");
            if (count <= 0) throw new InvalidBookingException("Requested room count must be greater than zero");
        }
    }

    // Booking service demonstrating fail-fast and graceful handling
    static class BookingService {
        private final Inventory inventory;

        public BookingService(Inventory inventory) { this.inventory = inventory; }

        public void processBooking(String guestName, String roomType, int count) {
            try {
                // Validate inputs first (fail-fast)
                BookingValidator.validateInput(guestName, roomType, count);

                // Check and reserve inventory (guard state)
                inventory.reserve(roomType, count);

                // If we reach here, booking is confirmed
                System.out.println("Booking confirmed for " + guestName + " -> " + count + " x " + roomType);
            } catch (InvalidBookingException e) {
                // Domain-specific validation error: show meaningful message
                System.out.println("Booking failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                // Programming/usage error (invalid args)
                System.out.println("Invalid request: " + e.getMessage());
            } catch (Exception e) {
                // Catch-all to keep system stable (log and continue)
                System.out.println("Unexpected error while processing booking: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Inventory inventory = new Inventory();
        BookingService service = new BookingService(inventory);

        System.out.println("Initial inventory: " + inventory.snapshot());

        // Valid booking
        service.processBooking("Charlie", "Single", 2);

        // Invalid room type
        service.processBooking("Dana", "Penthouse", 1);

        // Invalid count
        service.processBooking("Eve", "Double", 0);

        // Overbooking attempt (should be prevented)
        service.processBooking("Frank", "Suite", 2);

        // Another valid booking
        service.processBooking("Grace", "Double", 1);

        System.out.println("Final inventory: " + inventory.snapshot());
    }
}
