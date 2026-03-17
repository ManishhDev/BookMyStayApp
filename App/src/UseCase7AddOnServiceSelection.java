import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use Case 7: Add-On Service Selection.
 * Demonstrates optional service attachment without modifying core booking/allocation state.
 *
 * @author Manish
 * @version 7.0
 */
public class UseCase7AddOnServiceSelection {

    static class Reservation {
        private final String reservationId;
        private final String guestName;
        private final String roomType;

        public Reservation(String reservationId, String guestName, String roomType) {
            this.reservationId = reservationId;
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getReservationId() {
            return reservationId;
        }

        @Override
        public String toString() {
            return "Reservation{id='" + reservationId + "', guest='" + guestName
                + "', roomType='" + roomType + "'}";
        }
    }

    static class AddOnService {
        private final String serviceName;
        private final double cost;

        public AddOnService(String serviceName, double cost) {
            this.serviceName = serviceName;
            this.cost = cost;
        }

        public String getServiceName() {
            return serviceName;
        }

        public double getCost() {
            return cost;
        }

        @Override
        public String toString() {
            return serviceName + " ($" + cost + ")";
        }
    }

    static class AddOnServiceManager {
        private final Map<String, List<AddOnService>> reservationServices = new HashMap<>();

        public void addServiceToReservation(String reservationId, AddOnService service) {
            reservationServices.computeIfAbsent(reservationId, key -> new ArrayList<>()).add(service);
        }

        public List<AddOnService> getServicesForReservation(String reservationId) {
            return reservationServices.getOrDefault(reservationId, new ArrayList<>());
        }

        public double calculateAdditionalCost(String reservationId) {
            double total = 0.0;
            for (AddOnService service : getServicesForReservation(reservationId)) {
                total += service.getCost();
            }
            return total;
        }

        public void displayServices(String reservationId) {
            List<AddOnService> services = getServicesForReservation(reservationId);
            System.out.println("Selected Add-On Services for Reservation " + reservationId);
            System.out.println("----------------------------------------");

            if (services.isEmpty()) {
                System.out.println("No services selected.");
                return;
            }

            int count = 1;
            for (AddOnService service : services) {
                System.out.println(count + ". " + service);
                count++;
            }
            System.out.println("----------------------------------------");
            System.out.println("Total Additional Cost: $" + calculateAdditionalCost(reservationId));
         }
     }
 
     public static void main(String[] args) {
         System.out.println("==================================================");
         System.out.println("   BOOK MY STAY APP - ADD-ON SERVICE SELECTION    ");
         System.out.println("                 Version: 7.0                     ");
         System.out.println("==================================================");
 
         Reservation reservation = new Reservation("R-1001", "Aarav", "Suite Room");
         AddOnServiceManager addOnManager = new AddOnServiceManager();
 
         AddOnService breakfast = new AddOnService("Breakfast", 18.50);
         AddOnService airportPickup = new AddOnService("Airport Pickup", 35.00);
         AddOnService spaAccess = new AddOnService("Spa Access", 50.00);
 
         System.out.println("\nReservation Created:");
         System.out.println(reservation);
 
         System.out.println("\nGuest selects optional services...");
         addOnManager.addServiceToReservation(reservation.getReservationId(), breakfast);
         addOnManager.addServiceToReservation(reservation.getReservationId(), airportPickup);
         addOnManager.addServiceToReservation(reservation.getReservationId(), spaAccess);
 
         System.out.println();
         addOnManager.displayServices(reservation.getReservationId());
 
         System.out.println("\nCore booking and inventory state remain unchanged.");
         System.out.println("Application terminated successfully.");
     }
 }
