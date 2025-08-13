import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;

class FileBackedTaskManagerTest {
    @Test
    void shouldSaveAndLoadFromFile() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        Task task1 = new Task(1, "Test1", "Desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test2", "Desc2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getTask(task1.getId()).getId());
        assertEquals(2, loadedManager.getTask(task2.getId()).getId());
    }

    @Test
    void shouldUpdateIdGeneratorAfterLoading() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile, new InMemoryHistoryManager());
        Task task1 = new Task(1, "Test1", "Desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test2", "Desc2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task3 = new Task(1,"Test3", "Desc3", TaskStatus.NEW);
        loadedManager.createTask(task3);
        assertEquals(3, loadedManager.getTask(task3.getId()).getId());
    }
}
