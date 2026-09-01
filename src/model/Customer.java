package model;

/**
 * Customer role - can register, browse halls, book, pay, view/cancel
 * bookings and raise issues.
 */
public class Customer extends User {

    private String phoneNumber;

    public Customer(String userId, String username, String password, String fullName,
                     String email, String phoneNumber) {
        super(userId, username, password, fullName, email);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String toFileLine() {
        return super.toFileLine() + "|" + phoneNumber;
    }
}
