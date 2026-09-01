import io.FileManager;
import model.Issue;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class IssueFlowTest {
    public static void main(String[] args) {
        FileManager fm = new FileManager();
        AtomicInteger notified = new AtomicInteger();
        fm.addDataChangeListener(dataType -> {
            if ("issues".equals(dataType)) {
                notified.incrementAndGet();
            }
        });

        int before = fm.getAllIssues().size();
        Issue issue = new Issue(fm.nextIssueId(), "C1", "B1", "Door broken", Issue.Status.OPEN, null, LocalDateTime.now());
        issue.setManagerReply("We will inspect the door shortly.");
        fm.addIssue(issue);
        int after = fm.getAllIssues().size();
        if (after != before + 1) {
            throw new AssertionError("Issue was not stored correctly. Expected " + (before + 1) + " but got " + after);
        }
        Issue stored = fm.getAllIssues().get(fm.getAllIssues().size() - 1);
        if (!"We will inspect the door shortly.".equals(stored.getManagerReply())) {
            throw new AssertionError("Manager reply was not persisted.");
        }
        if (notified.get() != 1) {
            throw new AssertionError("Issue listeners were not notified.");
        }
        System.out.println("IssueFlowTest passed");
    }
}
