import java.util.StringJoiner;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CSVFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public String toString(Task task) {
        StringJoiner joiner = new StringJoiner(",");
        joiner.add(String.valueOf(task.getId()))
                .add(task.getType().name())
                .add(task.getName())
                .add(task.getStatus().name())
                .add(task.getDescription());

        if (task instanceof Subtask) {
            joiner.add(String.valueOf(((Subtask) task).getEpicId()));
        } else {
            joiner.add(""); // пустое поле для эпиков и обычных задач
        }

        joiner.add(task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "")
                .add(task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "");

        if (task instanceof Epic) {
            joiner.add(((Epic) task).getEndTime() != null ?
                    ((Epic) task).getEndTime().format(DATE_TIME_FORMATTER) : "");
        } else {
            joiner.add(""); //для не-эпиков
        }

        return joiner.toString();
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",", -1);
        if (parts.length < 6) return null;
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts.length > 5 ? parts[5] : "";
        Duration duration = parts.length > 6 && !parts[6].isEmpty() ?
                Duration.ofMinutes(Long.parseLong(parts[6])) : null;
        LocalDateTime startTime = parts.length > 7 && !parts[7].isEmpty() ?
                LocalDateTime.parse(parts[7], DATE_TIME_FORMATTER) : null;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                if (parts.length > 8 && !parts[8].isEmpty()) {
                    epic.setEndTime(LocalDateTime.parse(parts[8], DATE_TIME_FORMATTER));
                }
                return epic;
            case SUBTASK:
                int epicId = epicIdStr.isEmpty() ? 0 : Integer.parseInt(epicIdStr);
                return new Subtask(id, name, description, status, epicId, duration, startTime);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }
}
