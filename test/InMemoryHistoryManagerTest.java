import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    @Test
    void historyManagerShouldSaveOldAndNewVersionsOfSameTask() {
        TaskManager manager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task originalTask = new Task(1, "OriginalName", "OriginalDesc", TaskStatus.NEW);
        manager.createTask(originalTask);
        Task taskInManager = manager.getTask(1);
        Task firstVersion = manager.getHistory().get(0);

        Task updatedTask = new Task(1, "UpdatedName", "UpdatedDesc", TaskStatus.IN_PROGRESS);
        manager.updateTask(updatedTask);
        manager.getTask(1);

        assertEquals(2, manager.getHistory().size(), "Обе версии не сохранены");
        assertEquals("OriginalName", firstVersion.getName(), "Оригинальное имя не сохранилось");
        assertEquals("OriginalDesc", firstVersion.getDescription(), "Оригинальное описание не сохранилось");
        assertEquals(TaskStatus.NEW, firstVersion.getStatus(), "Оригинальный статус не сохранился");

        Task updatedVersion = manager.getHistory().get(1);
        assertEquals("UpdatedName", updatedVersion.getName(), "Обновленное имя не сохранилось");
        assertEquals("UpdatedDesc", updatedVersion.getDescription(), "Обновленное описание не сохранилось");
        assertEquals(TaskStatus.IN_PROGRESS, updatedVersion.getStatus(), "Обновленный статус не сохранился");

        assertEquals(firstVersion.getId(), updatedVersion.getId(), "Должны быть версии одной задачи");
    }
}