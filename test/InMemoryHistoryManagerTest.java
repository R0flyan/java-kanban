import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class InMemoryHistoryManagerTest {
    TaskManager manager = Managers.getDefault();
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    void shouldNotStoreDuplicatesInHistory() {

        Task task1 = new Task(1, "Task1", "Task1-desc", TaskStatus.NEW);
        Task task2 = new Task(2, "Task2", "Task2-desc", TaskStatus.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> expected = List.of(task2, task1);
        assertEquals(expected, historyManager.getHistory(), "В истории сохраняются дубликаты");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task = new Task(1, "Task", "desc", TaskStatus.NEW);
        historyManager.add(task);
        historyManager.remove(1);
        assertTrue(historyManager.getHistory().isEmpty(), "Задача не удаляется");
    }

    @Test
    void shouldChangeInsertionOrder() {
        Task task1 = new Task(1, "Task 1", "desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "desc2", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Должен переместиться в конец

        assertEquals(2, historyManager.getHistory().size(), "Добавляются лишнии задачи");
        assertEquals(task2, historyManager.getHistory().get(0));
        assertEquals(task1, historyManager.getHistory().get(1));
    }
}