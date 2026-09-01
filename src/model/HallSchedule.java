package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents either an AVAILABILITY window or a MAINTENANCE window that a
 * Scheduler has set for a specific hall, with an optional remark.
 */
public class HallSchedule implements Serializable {

    public enum Type { AVAILABILITY, MAINTENANCE }

    private String scheduleId;
    private String hallId;
    private Type type;
    private LocalDateTime start;
    private LocalDateTime end;
    private String remarks;

    public HallSchedule(String scheduleId, String hallId, Type type,
                         LocalDateTime start, LocalDateTime end, String remarks) {
        this.scheduleId = scheduleId;
        this.hallId = hallId;
        this.type = type;
        this.start = start;
        this.end = end;
        this.remarks = remarks;
    }

    public String getScheduleId() { return scheduleId; }
    public String getHallId() { return hallId; }
    public Type getType() { return type; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getRemarks() { return remarks; }

    /** True if the given range overlaps this schedule window at all. */
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return !otherEnd.isBefore(start) && !otherStart.isAfter(end) && otherStart.isBefore(end) && otherEnd.isAfter(start);
    }

    public String toFileLine() {
        return scheduleId + "|" + hallId + "|" + type + "|" + start + "|" + end + "|" +
                (remarks == null ? "" : remarks.replace("|", "/"));
    }

    @Override
    public String toString() {
        return type + " for " + hallId + ": " + start + " -> " + end +
                (remarks != null && !remarks.isEmpty() ? " (" + remarks + ")" : "");
    }
}
