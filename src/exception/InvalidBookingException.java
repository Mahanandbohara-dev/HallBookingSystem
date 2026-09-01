package exception;

/**
 * Thrown when a requested booking is invalid: outside business hours
 * (8:00-18:00), end before/equal to start, or the hall is already
 * booked / under maintenance for the requested slot.
 */
public class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}
