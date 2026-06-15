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
        } else if (areRowsAdjacent(rows)) {
            score += 250;
        } else {
            score -= 400;
        }

        return score;
    }

    public static boolean areRowsAdjacent(Set<Integer> rows) {
        if (rows.size() <= 1) {
            return true;
        }

        int minRow = Collections.min(rows);
        int maxRow = Collections.max(rows);

        return maxRow - minRow == 1;
    }

    public static String getReason(Passenger passenger, Seat seat) {
        List<String> reasons = new ArrayList<>();

        if (passenger.paidSeat != null &&
            passenger.paidSeat.equals(seat.seatNo)) {
            reasons.add("paid seat selection honored");
        }

        if (passenger.preference != null &&
            passenger.preference.equals(seat.type)) {
            reasons.add(passenger.preference + " preference matched");
        }

        if (passenger.preference != null &&
            passenger.preference.equals("front-row") && seat.frontRow) {
            reasons.add("front-row preference matched");
        }

        if (passenger.preference != null &&
            passenger.preference.equals("quiet-zone") && seat.quietZone) {
            reasons.add("quiet-zone preference matched");
        }

        if (passenger.preference != null &&
            passenger.preference.equals("extra-legroom") && seat.extraLegroom) {
            reasons.add("extra-legroom preference matched");
        }

        if (passenger.loyaltyPriority > 0) {
            reasons.add("loyalty priority considered");
        }

        if (passenger.checkInOrder > 0) {
            reasons.add("check-in order considered");
        }

        if (reasons.isEmpty()) {
            return "Valid seat assigned based on availability and rules.";
        }

        return String.join(", ", reasons) + ".";
    }

    public static List<Assignment> buildAssignmentsForSeats(
            List<Passenger> group,
            List<Seat> candidateSeats) {

        List<Assignment> assignments = new ArrayList<>();
        Set<String> tempUsedSeats = new HashSet<>();

        for (Passenger passenger : group) {
            Seat bestSeat = null;
            int bestScore = -1;

            for (Seat seat : candidateSeats) {
                if (!tempUsedSeats.contains(seat.seatNo) &&
                    isSeatValid(passenger, seat)) {

                    int currentScore = calculateSeatScore(passenger, seat);

                    if (currentScore > bestScore ||
                        (currentScore == bestScore &&
                         bestSeat != null &&
                         seat.seatNo.compareTo(bestSeat.seatNo) < 0)) {
                        bestScore = currentScore;
                        bestSeat = seat;
                    }
                }
            }

            if (bestSeat == null) {
                return null;
            }

            tempUsedSeats.add(bestSeat.seatNo);
            assignments.add(new Assignment(passenger, bestSeat, getReason(passenger, bestSeat)));
        }

        return assignments;
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

            List<Assignment> rowAssignments = buildAssignmentsForSeats(group, rowSeats);

            if (rowAssignments != null && rowAssignments.size() == group.size()) {
                int rowScore = calculateScore(rowAssignments);

                if (rowScore > bestRowScore ||
                    (rowScore == bestRowScore &&
                     isEarlierPlan(rowAssignments, bestRowAssignments))) {
                    bestRowScore = rowScore;
                    bestRowAssignments = rowAssignments;
                }
            }
        }

        return bestRowAssignments;
    }

    public static List<Assignment> assignGroupInAdjacentRows(
            List<Passenger> group,
            List<Seat> seats,
            Set<String> usedSeats) {

        Set<Integer> rowNumbers = new HashSet<>();

        for (Seat seat : seats) {
            if (!usedSeats.contains(seat.seatNo)) {
                rowNumbers.add(seat.row);
            }
        }

        List<Integer> sortedRows = new ArrayList<>(rowNumbers);
        Collections.sort(sortedRows);

        List<Assignment> bestAssignments = null;
        int bestScore = -1;

        for (int i = 0; i < sortedRows.size() - 1; i++) {
            int firstRow = sortedRows.get(i);
            int secondRow = sortedRows.get(i + 1);

            if (secondRow - firstRow != 1) {
                continue;
            }

            List<Seat> candidateSeats = new ArrayList<>();

            for (Seat seat : seats) {
                if (!usedSeats.contains(seat.seatNo) &&
                    (seat.row == firstRow || seat.row == secondRow)) {
                    candidateSeats.add(seat);
                }
            }

            if (candidateSeats.size() < group.size()) {
                continue;
            }

            List<Assignment> assignments = buildAssignmentsForSeats(group, candidateSeats);

            if (assignments != null && assignments.size() == group.size()) {
                int score = calculateScore(assignments);

                if (score > bestScore ||
                    (score == bestScore &&
                     isEarlierPlan(assignments, bestAssignments))) {
                    bestScore = score;
                    bestAssignments = assignments;
                }
            }
        }

        return bestAssignments;
    }

    public static List<Assignment> assignGroupInSameSection(
            List<Passenger> group,
            List<Seat> seats,
            Set<String> usedSeats) {

        List<Seat> candidateSeats = new ArrayList<>();

        for (Seat seat : seats) {
            if (!usedSeats.contains(seat.seatNo)) {
                candidateSeats.add(seat);
            }
        }

        if (candidateSeats.size() < group.size()) {
            return null;
        }

        return buildAssignmentsForSeats(group, candidateSeats);
    }

    public static boolean isEarlierPlan(List<Assignment> firstPlan, List<Assignment> secondPlan) {
        if (secondPlan == null) {
            return true;
        }

        String firstSeats = getSeatPlanKey(firstPlan);
        String secondSeats = getSeatPlanKey(secondPlan);

        return firstSeats.compareTo(secondSeats) < 0;
    }

    public static String getSeatPlanKey(List<Assignment> assignments) {
        List<String> seatNumbers = new ArrayList<>();

        for (Assignment assignment : assignments) {
            seatNumbers.add(assignment.seat.seatNo);
        }

        Collections.sort(seatNumbers);

        return String.join("-", seatNumbers);
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
                currentAssignments = assignGroupInAdjacentRows(group, seats, usedSeats);
            }

            if (currentAssignments == null) {
                currentAssignments = assignGroupInSameSection(group, seats, usedSeats);
            }

            if (currentAssignments == null) {
                System.out.println("No valid seat assignment found for group " + groupId);
                return finalAssignments;
            }

            for (Assignment assignment : currentAssignments) {
                usedSeats.add(assignment.seat.seatNo);
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