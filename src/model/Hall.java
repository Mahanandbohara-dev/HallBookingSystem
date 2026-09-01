package model;

import java.io.Serializable;

/**
 * Abstract base for all hall types. Capacity and hourly rate are fixed
 * per Table 1 of the assignment brief and are set by each concrete
 * subclass constructor - this is ABSTRACTION (the caller only ever
 * needs to know "this is a Hall") combined with POLYMORPHISM
 * (calculateCost / getHallType behave differently per subclass).
 */
public abstract class Hall implements Serializable {

    private String hallId;
    private String hallName;
    private final int capacity;
    private final double ratePerHour;
    private boolean active; // false = deleted (soft delete so bookings keep referential integrity)

    public Hall(String hallId, String hallName, int capacity, double ratePerHour) {
        this.hallId = hallId;
        this.hallName = hallName;
        this.capacity = capacity;
        this.ratePerHour = ratePerHour;
        this.active = true;
    }

    public abstract String getHallType();

    public double calculateCost(double hours) {
        return hours * ratePerHour;
    }

    public String getHallId() { return hallId; }
    public String getHallName() { return hallName; }
    public int getCapacity() { return capacity; }
    public double getRatePerHour() { return ratePerHour; }
    public boolean isActive() { return active; }

    public void setHallName(String hallName) { this.hallName = hallName; }
    public void setActive(boolean active) { this.active = active; }

    public String toFileLine() {
        return hallId + "|" + getHallType() + "|" + hallName + "|" + capacity + "|" + ratePerHour + "|" + active;
    }

    @Override
    public String toString() {
        return hallName + " [" + getHallType() + "] - capacity " + capacity + ", RM " + ratePerHour + "/hr";
    }
}
