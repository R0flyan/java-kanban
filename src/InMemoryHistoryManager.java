import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>(MAX_HISTORY_SIZE); // Используем Deque для эффективного управления историей

    @Override
    public void add(Task task) {
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0); // Удаляем самый старый элемент, если достигли лимита
        }
        history.add(task);   // Добавляем новую задачу в начало
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возвращаем копию, чтобы избежать изменений извне
    }
}
