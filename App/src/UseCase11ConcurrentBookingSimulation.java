import java.util.*;
import java.util.concurrent.*;

public class UseCase11ConcurrentBookingSimulation {

    static class Inventory {
        private final Map<String, Integer> counts = new HashMap<>();
        private final Map<String, Deque<String>> availableIds = new HashMap<>();

        public Inventory() {
            addRoomType("Single", Arrays.asList("S1","S2","S3","S4"));
            addRoomType("Double", Arrays.asList("D1","D2","D3"));
            addRoomType("Suite", Arrays.asList("SU1","SU2"));
        }

        private void addRoomType(String type, List<String> ids) {
            Deque<String> dq = new ArrayDeque<>();
            // push so pollFirst gives top-most (LIFO-like behavior if desired)
            for (String id : ids) dq.addFirst(id);
            availableIds.put(type, dq);
            counts.put(type, dq.size());
        }

        // Thread-safe allocate: returns allocated roomId or null if none
        public synchronized String allocate(String roomType) {
            Deque<String> dq = availableIds.get(roomType);
            if (dq == null || dq.isEmpty()) return null;
            String id = dq.removeFirst();
            counts.put(roomType, dq.size());
            return id;
        }

        // Thread-safe release
        public synchronized void release(String roomType, String roomId) {
            Deque<String> dq = availableIds.get(roomType);
            if (dq == null) {
                dq = new ArrayDeque<>();
                availableIds.put(roomType, dq);
            }
            dq.addFirst(roomId);
            counts.put(roomType, dq.size());
        }

        public synchronized Map<String,Integer> snapshotCounts() {
            return Collections.unmodifiableMap(new HashMap<>(counts));
        }
    }

    static class Reservation {
        final String id;
        final String guest;
        final String type;
        final String roomId;
        Reservation(String id, String guest, String type, String roomId) { this.id=id; this.guest=guest; this.type=type; this.roomId=roomId; }
        public String toString(){ return String.format("%s: %s -> %s (%s)", id, guest, type, roomId); }
    }

    static class BookingManager {
        private final Inventory inventory;
        private final Map<String,Reservation> reservations = new ConcurrentHashMap<>();

        public BookingManager(Inventory inv){ this.inventory = inv; }

        // confirmBooking must be thread-safe to avoid double allocation
        public Reservation confirmBooking(String id, String guest, String roomType){
            // allocate inside synchronized inventory.allocate
            String roomId = inventory.allocate(roomType);
            if (roomId == null) {
                return null;
            }
            Reservation r = new Reservation(id, guest, roomType, roomId);
            reservations.put(id, r);
            return r;
        }

        public Collection<Reservation> getAll() { return reservations.values(); }
    }

    public static void main(String[] args) throws Exception {
        Inventory inventory = new Inventory();
        BookingManager mgr = new BookingManager(inventory);

        System.out.println("Starting concurrent booking simulation");
        System.out.println("Initial inventory: " + inventory.snapshotCounts());

        // Prepare a set of booking requests
        List<Runnable> tasks = new ArrayList<>();
        String[] types = new String[]{"Single","Double","Suite"};
        for (int i=0;i<20;i++){
            final String id = "B" + (100 + i);
            final String guest = "Guest" + i;
            final String type = types[new Random(i).nextInt(types.length)];
            tasks.add(() -> {
                Reservation r = mgr.confirmBooking(id, guest, type);
                if (r != null) System.out.println("CONFIRMED: " + r);
                else System.out.println("REJECTED (no inventory): " + id + " -> " + type);
            });
        }

        // Run tasks concurrently
        ExecutorService ex = Executors.newFixedThreadPool(6);
        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (Runnable t : tasks){
            ex.submit(() -> { try { t.run(); } finally { latch.countDown(); } });
        }

        latch.await();
        ex.shutdown();

        System.out.println();
        System.out.println("Final reservations (" + mgr.getAll().size() + "):");
        mgr.getAll().forEach(r -> System.out.println(" - " + r));
        System.out.println("Final inventory: " + inventory.snapshotCounts());
    }
}
