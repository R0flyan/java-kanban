import java.util.StringJoiner;

public class CSVFormatter {

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

        return joiner.toString();
    }

    public static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts.length > 5 ? parts[5] : "";

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                Epic epic = new Epic(id, name, description);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = epicIdStr.isEmpty() ? 0 : Integer.parseInt(epicIdStr);
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }
}
