package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Booking implements Serializable {

    public enum Status { CONFIRMED, CANCELLED }

    private String bookingId;
    private String customerId;
    private String hallId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private double totalCost;
    private Status status;
    private LocalDateTime bookedOn;

    public Booking(String bookingId, String customerId, String hallId,
                    LocalDateTime startDateTime, LocalDateTime endDateTime,
                    double totalCost, Status status, LocalDateTime bookedOn) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.hallId = hallId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.totalCost = totalCost;
        this.status = status;
        this.bookedOn = bookedOn;
    }

    public String getBookingId() { return bookingId; }
    public String getCustomerId() { return customerId; }
    public String getHallId() { return hallId; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime() { return endDateTime; }
    public double getTotalCost() { return totalCost; }
    public Status getStatus() { return status; }
    public LocalDateTime getBookedOn() { return bookedOn; }

    public void setStatus(Status status) { this.status = status; }

    public String toFileLine() {
        return bookingId + "|" + customerId + "|" + hallId + "|" + startDateTime + "|" +
                endDateTime + "|" + totalCost + "|" + status + "|" + bookedOn;
    }

    @Override
    public String toString() {
        return bookingId + " (" + hallId + ", " + startDateTime + " - " + status + ")";
    }
}
