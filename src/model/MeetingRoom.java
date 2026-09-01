package model;

/** Fixed capacity 30, RM 50.00/hour, as per Table 1 of the brief. */
public class MeetingRoom extends Hall {

    public static final int CAPACITY = 30;
    public static final double RATE_PER_HOUR = 50.00;

    public MeetingRoom(String hallId, String hallName) {
        super(hallId, hallName, CAPACITY, RATE_PER_HOUR);
    }

    @Override
    public String getHallType() {
        return "MEETING_ROOM";
    }
}
