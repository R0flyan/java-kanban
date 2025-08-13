import java.util.List;
import java.util.ArrayList;

public class Epic extends Task {
    private List<Integer> subtaskIds;

    public Epic(int id, String name, String description) {
        super(id, name, description, TaskStatus.NEW);
        this.subtaskIds = new ArrayList<>();
        this.type = TaskType.EPIC;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId == this.getId()) {
            return;
        }
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}
