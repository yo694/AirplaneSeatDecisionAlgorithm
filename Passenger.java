public class Passenger {
    String id;
    String ageCategory;
    String ticketClass;
    String preference;
    String groupId;
    String paidSeat;

    public Passenger(String id, String ageCategory, String ticketClass,
                     String preference, String groupId, String paidSeat) {
        this.id = id;
        this.ageCategory = ageCategory;
        this.ticketClass = ticketClass;
        this.preference = preference;
        this.groupId = groupId;
        this.paidSeat = paidSeat;
    }
}