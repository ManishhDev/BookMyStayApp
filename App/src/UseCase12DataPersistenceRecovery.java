import java.io.*;
import java.util.*;

public class UseCase12DataPersistenceRecovery {

    static class Reservation implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String id;
        private final String guestName;
        private final String roomType;
        private final String roomId;

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

        @Override
        public String toString() {
            return String.format("Reservation[%s] Guest:%s Type:%s RoomID:%s", id, guestName, roomType, roomId);
        }
    }

    static class Inventory implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<String,Integer> counts = new HashMap<>();
        private final Map<String, Stack<String>> availableIds = new HashMap<>();

        public Inventory() {
            addRoomType("Single", Arrays.asList("S1","S2","S3"));
            addRoomType("Double", Arrays.asList("D1","D2"));
            addRoomType("Suite", Arrays.asList("SU1"));
        }

        private void addRoomType(String type, List<String> ids) {
            Stack<String> st = new Stack<>();
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
            st.push(roomId);
            counts.put(roomType, st.size());
        }

        public synchronized Map<String,Integer> snapshotCounts() {
            return Collections.unmodifiableMap(new HashMap<>(counts));
        }

        @Override
        public String toString() {
            return "Inventory counts: " + snapshotCounts();
        }
    }

    static class BookingManager implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Inventory inventory;
        private final Map<String, Reservation> reservations = new LinkedHashMap<>();

        public BookingManager(Inventory inventory) {
            this.inventory = inventory;
        }

        public Reservation confirmBooking(String id, String guest, String roomType) {
            String roomId = inventory.allocate(roomType);
            if (roomId == null) return null;
            Reservation r = new Reservation(id, guest, roomType, roomId);
            reservations.put(id, r);
            return r;
        }

        public Collection<Reservation> getAllReservations() { return reservations.values(); }

        public Inventory getInventory() { return inventory; }

        @Override
        public String toString() {
            return "Manager: reservations=" + reservations.size() + " , " + inventory.toString();
        }
    }

    static class PersistenceService {
        public static void save(String path, BookingManager mgr) throws IOException {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
                oos.writeObject(mgr);
            }
        }

        public static BookingManager load(String path) throws IOException, ClassNotFoundException {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
                Object obj = ois.readObject();
                if (obj instanceof BookingManager) return (BookingManager) obj;
                throw new IOException("Unexpected persisted type: " + obj.getClass());
            }
        }
    }

    public static void main(String[] args) {
        String stateFile = "booking_state.bin";

        // Step 1: Build initial state and persist it
        BookingManager mgr = new BookingManager(new Inventory());
        mgr.confirmBooking("R201","Alice","Single");
        mgr.confirmBooking("R202","Bob","Double");

        System.out.println("Before saving: ");
        mgr.getAllReservations().forEach(r -> System.out.println(" - " + r));
        System.out.println(mgr.getInventory());

        try {
            PersistenceService.save(stateFile, mgr);
            System.out.println("State saved to " + stateFile);
        } catch (IOException e) {
            System.out.println("Failed to save state: " + e.getMessage());
        }

        // Simulate application restart by loading state from file
        System.out.println();
        System.out.println("Simulating restart: loading persisted state...");

        BookingManager restored = null;
        try {
            restored = PersistenceService.load(stateFile);
            System.out.println("Restored manager: " + restored);
            restored.getAllReservations().forEach(r -> System.out.println(" - " + r));
        } catch (FileNotFoundException e) {
            System.out.println("No persisted state found, starting fresh");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Failed to restore persisted state (corrupt or incompatible). Starting fresh. Error: " + e.getMessage());
            restored = new BookingManager(new Inventory());
        }

        // Show that restored manager can continue operating
        System.out.println();
        System.out.println("Continuing operation after restore — confirming another booking");
        BookingManager active = (restored != null) ? restored : new BookingManager(new Inventory());
        Reservation newRes = active.confirmBooking("R203","Carol","Single");
        if (newRes != null) System.out.println("Confirmed after restore: " + newRes);
        else System.out.println("Could not confirm after restore — no inventory");

        // Save updated state
        try {
            PersistenceService.save(stateFile, active);
            System.out.println("Updated state saved to " + stateFile);
        } catch (IOException e) {
            System.out.println("Failed to save updated state: " + e.getMessage());
        }
    }
}
