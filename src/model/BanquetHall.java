package model;

/** Fixed capacity 300, RM 100.00/hour, as per Table 1 of the brief. */
public class BanquetHall extends Hall {

    public static final int CAPACITY = 300;
    public static final double RATE_PER_HOUR = 100.00;

    public BanquetHall(String hallId, String hallName) {
        super(hallId, hallName, CAPACITY, RATE_PER_HOUR);
    }

    @Override
    public String getHallType() {
        return "BANQUET_HALL";
    }
}
