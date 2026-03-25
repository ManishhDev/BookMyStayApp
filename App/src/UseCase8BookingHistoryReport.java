import java.util.*;

public class UseCase8BookingHistoryReport {

    static class Reservation {
        private final String id;
        private final String guestName;
        private final int roomNumber;
        private final String checkInDate;
        private final String checkOutDate;
        private final double totalAmount;

        public Reservation(String id, String guestName, int roomNumber, String checkInDate, String checkOutDate, double totalAmount) {
            this.id = id;
            this.guestName = guestName;
            this.roomNumber = roomNumber;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.totalAmount = totalAmount;
        }

        public String getId() { return id; }
        public String getGuestName() { return guestName; }
        public int getRoomNumber() { return roomNumber; }
        public String getCheckInDate() { return checkInDate; }
        public String getCheckOutDate() { return checkOutDate; }
        public double getTotalAmount() { return totalAmount; }

        @Override
        public String toString() {
            return String.format("Reservation[%s] Guest:%s Room:%d %s->%s Amount:%.2f",
                    id, guestName, roomNumber, checkInDate, checkOutDate, totalAmount);
        }
    }

    static class BookingHistory {
        private final List<Reservation> confirmed = new ArrayList<>();

        public void addConfirmedReservation(Reservation r) {
            if (r == null) throw new IllegalArgumentException("reservation cannot be null");
            confirmed.add(r);
        }

        public List<Reservation> getAllReservations() {
            return Collections.unmodifiableList(confirmed);
        }

        public boolean isEmpty() { return confirmed.isEmpty(); }
    }

    static class BookingReportService {
        public static String generateSummaryReport(BookingHistory history) {
            List<Reservation> list = history.getAllReservations();
            int totalBookings = list.size();
            double totalRevenue = 0.0;
            Map<String, Integer> bookingsPerGuest = new HashMap<>();

            for (Reservation r : list) {
                totalRevenue += r.getTotalAmount();
                bookingsPerGuest.merge(r.getGuestName(), 1, Integer::sum);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Booking History Report ===\n");
            sb.append("Total confirmed bookings: ").append(totalBookings).append('\n');
            sb.append(String.format("Total revenue: %.2f\n", totalRevenue));
            sb.append("Bookings per guest:\n");
            for (Map.Entry<String,Integer> e : bookingsPerGuest.entrySet()) {
                sb.append(" - ").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        BookingHistory history = new BookingHistory();

        // Sample confirmed bookings (in insertion order)
        history.addConfirmedReservation(new Reservation("R001","Alice",101,"2026-03-01","2026-03-03",300.00));
        history.addConfirmedReservation(new Reservation("R002","Bob",202,"2026-03-02","2026-03-05",450.00));
        history.addConfirmedReservation(new Reservation("R003","Alice",103,"2026-03-10","2026-03-12",250.00));

        // Administrator viewing booking history
        System.out.println("Stored reservations (chronological):");
        for (Reservation r : history.getAllReservations()) {
            System.out.println(r);
        }

        // Generate a summary report (non-destructive)
        String report = BookingReportService.generateSummaryReport(history);
        System.out.println();
        System.out.println(report);
    }
}
