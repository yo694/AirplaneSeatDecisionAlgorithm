public class Passenger {
    String id;
    String ageCategory;
    String ticketClass;
    String preference;
    String groupId;

    public Passenger(String id, String ageCategory, String ticketClass,
                     String preference, String groupId) {
        this.id = id;
        this.ageCategory = ageCategory;
        this.ticketClass = ticketClass;
        this.preference = preference;
        this.groupId = groupId;
    }
}