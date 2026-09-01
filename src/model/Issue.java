package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Issue implements Serializable {

    public enum Status { OPEN, IN_PROGRESS, DONE, CLOSED, CANCELLED }

    private String issueId;
    private String customerId;
    private String bookingId;
    private String description;
    private Status status;
    private String assignedSchedulerId; // may be null until Manager assigns
    private LocalDateTime raisedOn;
    private String managerReply;

    public Issue(String issueId, String customerId, String bookingId, String description,
                 Status status, String assignedSchedulerId, LocalDateTime raisedOn) {
        this(issueId, customerId, bookingId, description, status, assignedSchedulerId, raisedOn, null);
    }

    public Issue(String issueId, String customerId, String bookingId, String description,
                 Status status, String assignedSchedulerId, LocalDateTime raisedOn, String managerReply) {
        this.issueId = issueId;
        this.customerId = customerId;
        this.bookingId = bookingId;
        this.description = description;
        this.status = status;
        this.assignedSchedulerId = assignedSchedulerId;
        this.raisedOn = raisedOn;
        this.managerReply = managerReply;
    }

    public String getIssueId() { return issueId; }
    public String getCustomerId() { return customerId; }
    public String getBookingId() { return bookingId; }
    public String getDescription() { return description; }
    public Status getStatus() { return status; }
    public String getAssignedSchedulerId() { return assignedSchedulerId; }
    public LocalDateTime getRaisedOn() { return raisedOn; }
    public String getManagerReply() { return managerReply; }

    public void setStatus(Status status) { this.status = status; }
    public void setAssignedSchedulerId(String assignedSchedulerId) { this.assignedSchedulerId = assignedSchedulerId; }
    public void setManagerReply(String managerReply) { this.managerReply = managerReply; }

    public String toFileLine() {
        return issueId + "|" + customerId + "|" + bookingId + "|" + description.replace("|", "/") + "|" +
                status + "|" + (assignedSchedulerId == null ? "" : assignedSchedulerId) + "|" + raisedOn + "|" +
                (managerReply == null ? "" : managerReply.replace("|", "/"));
    }
}
