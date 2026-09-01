package exception;

/** Thrown when a customer tries to cancel a booking less than 3 days before its date. */
public class CancellationNotAllowedException extends Exception {
    public CancellationNotAllowedException(String message) {
        super(message);
    }
}
