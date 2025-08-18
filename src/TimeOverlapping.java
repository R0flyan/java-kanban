import java.time.LocalDateTime;

public class TimeOverlapping {
    public static boolean isTimeOverlapping(Task task1, Task task2) {
        if (task1 == null || task2 == null
                || task1.getStartTime() == null || task2.getStartTime() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
