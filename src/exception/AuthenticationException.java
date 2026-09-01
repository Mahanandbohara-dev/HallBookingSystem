package exception;

/** Thrown when login credentials are invalid or the account is blocked. */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
