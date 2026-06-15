import java.util.*;

public class AirplaneSeatAssignment {

    public static boolean isSeatValid(Passenger passenger, Seat seat) {
        if (!seat.available) {
            return false;
        }

        if (seat.status.equals("occupied") ||
            seat.status.equals("blocked") ||
            seat.status.equals("locked")) {
            return false;
        }

        if (seat.paidOnly &&
            (passenger.paidSeat == null || !passenger.paidSeat.equals(seat.seatNo))) {
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

            if (passenger.paidSeat != null &&
                passenger.paidSeat.equals(seat.seatNo)) {
                score += 300;
            }
        }

        return score;
    }

    public static String getReason(Passenger passenger, Seat seat) {
        if (passenger.paidSeat != null &&
            passenger.paidSeat.equals(seat.seatNo)) {
            return "Paid seat selection honored.";
        }

        if (passenger.preference != null &&
            passenger.preference.equals(seat.type)) {
            return passenger.preference + " preference matched.";
        }

        return "Valid seat assigned based on availability and rules.";
    }

    public static int calculateSeatScore(Passenger passenger, Seat seat) {
        int score = 0;

        if (passenger.ticketClass.equals(seat.seatClass)) {
            score += 500;
        }

        if (passenger.preference != null &&
            passenger.preference.equals(seat.type)) {
            score += 200;
        }

        if (passenger.paidSeat != null &&
            passenger.paidSeat.equals(seat.seatNo)) {
            score += 300;
        }

        return score;
    }

    public static List<Assignment> assignGroupInSameRow(
            List<Passenger> group,
            List<Seat> seats,
            Set<String> usedSeats) {

        Map<Integer, List<Seat>> seatsByRow = new HashMap<>();

        for (Seat seat : seats) {
            if (!usedSeats.contains(seat.seatNo)) {
                if (!seatsByRow.containsKey(seat.row)) {
                    seatsByRow.put(seat.row, new ArrayList<>());
                }

                seatsByRow.get(seat.row).add(seat);
            }
        }

        for (Integer row : seatsByRow.keySet()) {
            List<Seat> rowSeats = seatsByRow.get(row);

            if (rowSeats.size() < group.size()) {
                continue;
            }

            List<Assignment> rowAssignments = new ArrayList<>();
            Set<String> tempUsedSeats = new HashSet<>();

            for (Passenger passenger : group) {
                Seat bestSeat = null;
                int bestScore = -1;

                for (Seat seat : rowSeats) {
                    if (!tempUsedSeats.contains(seat.seatNo) &&
                        isSeatValid(passenger, seat)) {

                        int currentScore = calculateSeatScore(passenger, seat);

                        if (currentScore > bestScore) {
                            bestScore = currentScore;
                            bestSeat = seat;
                        }
                    }
                }

                if (bestSeat == null) {
                    rowAssignments.clear();
                    break;
                }

                tempUsedSeats.add(bestSeat.seatNo);

                rowAssignments.add(
                    new Assignment(passenger, bestSeat, getReason(passenger, bestSeat))
                );
            }

            if (rowAssignments.size() == group.size()) {
                return rowAssignments;
            }
        }

        return null;
    }

    public static List<Assignment> assignSeats(List<Passenger> passengers, List<Seat> seats) {
        List<Assignment> finalAssignments = new ArrayList<>();
        Set<String> usedSeats = new HashSet<>();

        Map<String, List<Passenger>> groups = groupPassengers(passengers);

        for (String groupId : groups.keySet()) {
            List<Passenger> group = groups.get(groupId);

            List<Assignment> currentAssignments = assignGroupInSameRow(group, seats, usedSeats);

            if (currentAssignments == null) {
                currentAssignments = new ArrayList<>();

                for (Passenger passenger : group) {
                    Seat selectedSeat = null;
                    int bestScore = -1;

                    for (Seat seat : seats) {
                        if (!usedSeats.contains(seat.seatNo) &&
                            isSeatValid(passenger, seat)) {

                            int currentScore = calculateSeatScore(passenger, seat);

                            if (currentScore > bestScore) {
                                bestScore = currentScore;
                                selectedSeat = seat;
                            }
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
            } else {
                for (Assignment assignment : currentAssignments) {
                    usedSeats.add(assignment.seat.seatNo);
                }
            }

            finalAssignments.addAll(currentAssignments);
        }

        return finalAssignments;
    }

    public static void main(String[] args) {
        List<Seat> seats = new ArrayList<>();

        seats.add(new Seat("10A", 10, "window", "economy", true, false, true, "available", true));
        seats.add(new Seat("10B", 10, "middle", "economy", true, false, true, "available", false));
        seats.add(new Seat("10C", 10, "aisle", "economy", true, false, true, "available", false));
        seats.add(new Seat("11A", 11, "window", "economy", true, false, true, "blocked", false));
        seats.add(new Seat("11B", 11, "middle", "economy", true, false, true, "available", false));
        seats.add(new Seat("11C", 11, "aisle", "economy", true, false, true, "available", false));

        List<Passenger> passengers = new ArrayList<>();

        passengers.add(new Passenger("P1", "adult", "economy", "window", "G1", "10A"));
        passengers.add(new Passenger("P2", "adult", "economy", "aisle", "G1", null));
        passengers.add(new Passenger("P3", "child", "economy", "none", "G1", null));

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