import java.util.*;

public class UseCase10BookingCancellation {

    static class Reservation {
        private final String id;
        private final String guestName;
        private final String roomType;
        private final String roomId;
        private boolean cancelled = false;

        public Reservation(String id, String guestName, String roomType, String roomId) {
            this.id = id;
            this.guestName = guestName;
            this.roomType = roomType;
            this.roomId = roomId;
        }

        public String getId() { return id; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public String getRoomId() { return roomId; }
        public boolean isCancelled() { return cancelled; }
        public void markCancelled() { cancelled = true; }

        @Override
        public String toString() {
            return String.format("Reservation[%s] Guest:%s Type:%s RoomID:%s Cancelled:%s",
                    id, guestName, roomType, roomId, cancelled);
        }
    }

    static class Inventory {
        private final Map<String, Integer> counts = new HashMap<>();
        private final Map<String, Stack<String>> availableIds = new HashMap<>();

        public Inventory() {
            // initialize sample rooms and ids
            addRoomType("Single", Arrays.asList("S1","S2","S3"));
            addRoomType("Double", Arrays.asList("D1","D2"));
            addRoomType("Suite", Arrays.asList("SU1"));
        }

        private void addRoomType(String type, List<String> ids) {
            Stack<String> st = new Stack<>();
            // push in listed order so top is the last element
            for (String id : ids) st.push(id);
            availableIds.put(type, st);
            counts.put(type, st.size());
        }

        public synchronized String allocate(String roomType) {
            Stack<String> st = availableIds.get(roomType);
            if (st == null || st.isEmpty()) return null;
            String id = st.pop();
            counts.put(roomType, st.size());
            return id;
        }

        public synchronized void release(String roomType, String roomId) {
            Stack<String> st = availableIds.get(roomType);
            if (st == null) {
                st = new Stack<>();
                availableIds.put(roomType, st);
            }
            st.push(roomId); // LIFO: release onto stack
            counts.put(roomType, st.size());
        }

        public synchronized int availableCount(String roomType) {
            return counts.getOrDefault(roomType, 0);
        }

        public synchronized Map<String,Integer> snapshotCounts() {
            return Collections.unmodifiableMap(new HashMap<>(counts));
        }

        public synchronized Map<String,List<String>> snapshotIds() {
            Map<String,List<String>> m = new HashMap<>();
            for (Map.Entry<String,Stack<String>> e : availableIds.entrySet()) {
                // show top-first order
                List<String> list = new ArrayList<>(e.getValue());
                Collections.reverse(list); // so top-of-stack shows first
                m.put(e.getKey(), list);
            }
            return m;
        }
    }

    static class BookingManager {
        private final Inventory inventory;
        private final Map<String, Reservation> reservations = new LinkedHashMap<>(); // preserve insertion order

        public BookingManager(Inventory inventory) { this.inventory = inventory; }

        public Reservation confirmBooking(String id, String guestName, String roomType) {
            String roomId = inventory.allocate(roomType);
            if (roomId == null) {
                System.out.println("No available rooms of type: " + roomType + " for booking " + id);
                return null;
            }
            Reservation r = new Reservation(id, guestName, roomType, roomId);
            reservations.put(id, r);
            System.out.println("Booking confirmed: " + r);
            return r;
        }

        public void cancelBooking(String id) {
            Reservation r = reservations.get(id);
            if (r == null) {
                System.out.println("Cancellation failed: reservation not found: " + id);
                return;
            }
            if (r.isCancelled()) {
                System.out.println("Cancellation failed: reservation already cancelled: " + id);
                return;
            }
            // record rollback (LIFO) by releasing the allocated room id
            inventory.release(r.getRoomType(), r.getRoomId());
            r.markCancelled();
            System.out.println("Cancellation successful: " + r);
        }

        public void printReservations() {
            System.out.println("Current reservations:");
            for (Reservation r : reservations.values()) System.out.println(" - " + r);
        }
    }

    public static void main(String[] args) {
        Inventory inv = new Inventory();
        BookingManager mgr = new BookingManager(inv);

        System.out.println("Initial inventory counts: " + inv.snapshotCounts());
        System.out.println("Initial available ids (top-first): " + inv.snapshotIds());

        // Confirm bookings
        mgr.confirmBooking("R100","Alice","Single");
        mgr.confirmBooking("R101","Bob","Single");
        mgr.confirmBooking("R102","Carol","Double");

        System.out.println();
        mgr.printReservations();
        System.out.println("Inventory after bookings: " + inv.snapshotCounts());
        System.out.println("Available ids after bookings (top-first): " + inv.snapshotIds());

        System.out.println();
        // Cancel the most recent Single booking (R101)
        mgr.cancelBooking("R101");

        // Attempt to cancel a non-existent booking
        mgr.cancelBooking("R999");

        // Attempt to cancel again the same booking
        mgr.cancelBooking("R101");

        System.out.println();
        mgr.printReservations();
        System.out.println("Inventory after cancellations: " + inv.snapshotCounts());
        System.out.println("Available ids after cancellations (top-first): " + inv.snapshotIds());
    }
}
