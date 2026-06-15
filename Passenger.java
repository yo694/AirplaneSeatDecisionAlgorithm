public class Passenger {
    String id;
    String ageCategory;
    String ticketClass;
    String preference;
    String groupId;
    String paidSeat;
    int loyaltyPriority;
    int checkInOrder;

    public Passenger(String id, String ageCategory, String ticketClass,
                     String preference, String groupId, String paidSeat,
                     int loyaltyPriority, int checkInOrder) {
        this.id = id;
        this.ageCategory = ageCategory;
        this.ticketClass = ticketClass;
        this.preference = preference;
        this.groupId = groupId;
        this.paidSeat = paidSeat;
        this.loyaltyPriority = loyaltyPriority;
        this.checkInOrder = checkInOrder;
    }
}