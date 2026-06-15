public class Seat {
    String seatNo;
    int row;
    String type;
    String seatClass;
    boolean available;
    boolean exitRow;
    boolean infantAllowed;
    String status;
    boolean paidOnly;
    boolean frontRow;
    boolean quietZone;
    boolean extraLegroom;

    public Seat(String seatNo, int row, String type, String seatClass,
                boolean available, boolean exitRow, boolean infantAllowed,
                String status, boolean paidOnly,
                boolean frontRow, boolean quietZone, boolean extraLegroom) {
        this.seatNo = seatNo;
        this.row = row;
        this.type = type;
        this.seatClass = seatClass;
        this.available = available;
        this.exitRow = exitRow;
        this.infantAllowed = infantAllowed;
        this.status = status;
        this.paidOnly = paidOnly;
        this.frontRow = frontRow;
        this.quietZone = quietZone;
        this.extraLegroom = extraLegroom;
    }
}