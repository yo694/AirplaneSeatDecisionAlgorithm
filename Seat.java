public class Seat {
    String seatNo;
    int row;
    String type;
    String seatClass;
    boolean available;
    boolean exitRow;
    boolean infantAllowed;

    public Seat(String seatNo, int row, String type, String seatClass,
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