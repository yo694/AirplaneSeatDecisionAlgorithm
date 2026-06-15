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

    public static void sortGroupByPriority(List<Passenger> group) {
        group.sort((p1, p2) -> {
            if (p1.loyaltyPriority != p2.loyaltyPriority) {
                return Integer.compare(p2.loyaltyPriority, p1.loyaltyPriority);
            }

            return Integer.compare(p1.checkInOrder, p2.checkInOrder);
        });
    }

    public static int calculateSeatScore(Passenger passenger, Seat seat) {
        int score = 0;

        if (passenger.ticketClass.equals(seat.seatClass)) {
            score += 500;
        }

        if (passenger.preference != null) {
            if (passenger.preference.equals(seat.type)) {
                score += 200;
            }

            if (passenger.preference.equals("front-row") && seat.frontRow) {
                score += 200;
            }

            if (passenger.preference.equals("quiet-zone") && seat.quietZone) {
                score += 200;
            }

            if (passenger.preference.equals("extra-legroom") && seat.extraLegroom) {
                score += 200;
            }
        }

        if (passenger.paidSeat != null &&
            passenger.paidSeat.equals(seat.seatNo)) {
            score += 300;
        }

        score += passenger.loyaltyPriority * 50;

        if (passenger.checkInOrder > 0) {
            score += Math.max(0, 100 - passenger.checkInOrder * 10);
        }

        return score;
    }

    public static int calculateScore(List<Assignment> assignments) {
        int score = 0;
        Set<Integer> rows = new HashSet<>();

        for (Assignment assignment : assignments) {
            rows.add(assignment.seat.row);
            score += calculateSeatScore(assignment.passenger, assignment.seat);
        }

        if (rows.size() == 1) {
            score += 400;
        } else {
            score -= 400;
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

        if (passenger.preference != null &&
            passenger.preference.equals("front-row") && seat.frontRow) {
            return "Front-row preference matched.";
        }

        if (passenger.preference != null &&
            passenger.preference.equals("quiet-zone") && seat.quietZone) {
            return "Quiet-zone preference matched.";
        }

        if (passenger.preference != null &&
            passenger.preference.equals("extra-legroom") && seat.extraLegroom) {
            return "Extra-legroom preference matched.";
        }

        if (passenger.loyaltyPriority > 0) {
            return "Valid seat assigned using loyalty and check-in priority.";
        }

        return "Valid seat assigned based on availability and rules.";
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

        List<Assignment> bestRowAssignments = null;
        int bestRowScore = -1;

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
                int rowScore = calculateScore(rowAssignments);

                if (rowScore > bestRowScore) {
                    bestRowScore = rowScore;
                    bestRowAssignments = rowAssignments;
                }
            }
        }

        return bestRowAssignments;
    }

    public static List<Assignment> assignSeats(List<Passenger> passengers, List<Seat> seats) {
        List<Assignment> finalAssignments = new ArrayList<>();
        Set<String> usedSeats = new HashSet<>();

        Map<String, List<Passenger>> groups = groupPassengers(passengers);

        for (String groupId : groups.keySet()) {
            List<Passenger> group = groups.get(groupId);
            sortGroupByPriority(group);

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

        seats.add(new Seat("10A", 10, "window", "economy", true, false, true, "available", true, true, false, true));
        seats.add(new Seat("10B", 10, "middle", "economy", true, false, true, "available", false, true, false, false));
        seats.add(new Seat("10C", 10, "aisle", "economy", true, false, true, "available", false, true, false, false));
        seats.add(new Seat("11A", 11, "window", "economy", true, false, true, "blocked", false, false, true, false));
        seats.add(new Seat("11B", 11, "middle", "economy", true, false, true, "available", false, false, true, false));
        seats.add(new Seat("11C", 11, "aisle", "economy", true, false, true, "available", false, false, true, false));
        seats.add(new Seat("12A", 12, "window", "economy", true, false, true, "available", false, false, false, true));
        seats.add(new Seat("12B", 12, "middle", "economy", true, false, true, "available", false, false, false, false));
        seats.add(new Seat("12C", 12, "aisle", "economy", true, false, true, "available", false, false, false, false));

        List<Passenger> passengers = new ArrayList<>();

        passengers.add(new Passenger("P1", "adult", "economy", "window", "G1", "10A", 3, 2));
        passengers.add(new Passenger("P2", "adult", "economy", "aisle", "G1", null, 2, 1));
        passengers.add(new Passenger("P3", "child", "economy", "none", "G1", null, 0, 3));
        passengers.add(new Passenger("P4", "adult", "economy", "extra-legroom", "G2", null, 5, 1));

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