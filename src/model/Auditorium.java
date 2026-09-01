package model;

/** Fixed capacity 1000, RM 300.00/hour, as per Table 1 of the brief. */
public class Auditorium extends Hall {

    public static final int CAPACITY = 1000;
    public static final double RATE_PER_HOUR = 300.00;

    public Auditorium(String hallId, String hallName) {
        super(hallId, hallName, CAPACITY, RATE_PER_HOUR);
    }

    @Override
    public String getHallType() {
        return "AUDITORIUM";
    }
}
