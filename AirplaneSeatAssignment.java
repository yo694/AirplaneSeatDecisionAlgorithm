import java.util.*;

public class AirplaneSeatAssignment {

    static class Seat {
        String seatNo;
        int row;
        String type;
        String seatClass;
        boolean available;
        boolean exitRow;
        boolean infantAllowed;

        Seat(String seatNo, int row, String type, String seatClass,
             boolean available, boolean exitRow, boolean infantAllowed) {
            this.seatNo = seatNo;
            this.row = row;
            this.type = type;
            this.seatClass = seatClass;
            this.available = available;
            this.exitRow = exitRow;
            this.infantAllowed = infantAllowed;
        }
    }

    static class Passenger {
        String id;
        String ageCategory;
        String ticketClass;
        String preference;
        String groupId;

        Passenger(String id, String ageCategory, String ticketClass,
                  String preference, String groupId) {
            this.id = id;
            this.ageCategory = ageCategory;
            this.ticketClass = ticketClass;
            this.preference = preference;
            this.groupId = groupId;
        }
    }

    static class Assignment {
        Passenger passenger;
        Seat seat;
        String reason;

        Assignment(Passenger passenger, Seat seat, String reason) {
            this.passenger = passenger;
            this.seat = seat;
            this.reason = reason;
        }
    }

    public static boolean isSeatValid(Passenger passenger, Seat seat) {
        if (!seat.available) {
            return false;
        }

        if ((passenger.ageCategory.equals("child") ||
             passenger.ageCategory.equals("infant")) && seat.exitRow) {
            return false;
        }

        if (passenger.ageCategory.equals("infant") && !seat.infantAllowed) {
            return false;
        }

        if (!passenger.ticketClass.equals(seat.seatClass)) {
            return false;
        }

        return true;
    }

    public static Map<String, List<Passenger>> groupPassengers(List<Passenger> passengers) {
        Map<String, List<Passenger>> groups = new HashMap<>();

        for (Passenger passenger : passengers) {
            String groupId = passenger.groupId;

            if (!groups.containsKey(groupId)) {
                groups.put(groupId, new ArrayList<>());
            }

            groups.get(groupId).add(passenger);
        }

        return groups;
    }

    public static int calculateScore(List<Assignment> assignments) {
        int score = 0;

        Set<Integer> rows = new HashSet<>();
        for (Assignment assignment : assignments) {
            rows.add(assignment.seat.row);
        }

        if (rows.size() == 1) {
            score += 400;
        } else {
            score -= 400;
        }

        for (Assignment assignment : assignments) {
            Passenger passenger = assignment.passenger;
            Seat seat = assignment.seat;

            if (passenger.preference != null &&
                passenger.preference.equals(seat.type)) {
                score += 200;
            } else if (passenger.preference != null &&
                       !passenger.preference.equals("none")) {
                score -= 50;
            }
        }

        return score;
    }

    public static String getReason(Passenger passenger, Seat seat) {
        if (passenger.preference != null &&
            passenger.preference.equals(seat.type)) {
            return passenger.preference + " preference matched.";
        }

        return "Valid seat assigned based on availability and rules.";
    }

    public static List<Assignment> assignSeats(List<Passenger> passengers, List<Seat> seats) {
        List<Assignment> finalAssignments = new ArrayList<>();
        Set<String> usedSeats = new HashSet<>();

        Map<String, List<Passenger>> groups = groupPassengers(passengers);

        for (String groupId : groups.keySet()) {
            List<Passenger> group = groups.get(groupId);

            List<Assignment> currentAssignments = new ArrayList<>();

            for (Passenger passenger : group) {
                Seat selectedSeat = null;

                for (Seat seat : seats) {
                    if (!usedSeats.contains(seat.seatNo) &&
                        isSeatValid(passenger, seat)) {
                        selectedSeat = seat;
                        break;
                    }
                }

                if (selectedSeat == null) {
                    System.out.println("No valid seat found for passenger " + passenger.id);
                    return finalAssignments;
                }

                usedSeats.add(selectedSeat.seatNo);
                currentAssignments.add(
                    new Assignment(passenger, selectedSeat, getReason(passenger, selectedSeat))
                );
            }

            finalAssignments.addAll(currentAssignments);
        }

        return finalAssignments;
    }

    public static void main(String[] args) {
        List<Seat> seats = new ArrayList<>();

        seats.add(new Seat("10A", 10, "window", "economy", true, false, true));
        seats.add(new Seat("10B", 10, "middle", "economy", true, false, true));
        seats.add(new Seat("10C", 10, "aisle", "economy", true, false, true));
        seats.add(new Seat("11A", 11, "window", "economy", true, false, true));
        seats.add(new Seat("11B", 11, "middle", "economy", true, false, true));
        seats.add(new Seat("11C", 11, "aisle", "economy", true, false, true));

        List<Passenger> passengers = new ArrayList<>();

        passengers.add(new Passenger("P1", "adult", "economy", "window", "G1"));
        passengers.add(new Passenger("P2", "adult", "economy", "aisle", "G1"));
        passengers.add(new Passenger("P3", "child", "economy", "none", "G1"));

        List<Assignment> result = assignSeats(passengers, seats);

        System.out.println("Final Seat Assignments:");
        System.out.println("-----------------------");

        for (Assignment assignment : result) {
            System.out.println(
                assignment.passenger.id + " -> " +
                assignment.seat.seatNo + " | " +
                assignment.reason
            );
        }
    }
}