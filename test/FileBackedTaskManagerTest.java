import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @Override
    protected FileBackedTaskManager createTaskManager() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
        return new FileBackedTaskManager(tempFile, Managers.getDefaultHistory());
    }

    @Test
    void shouldSaveAndLoadFromFile() throws IOException {
        Task task1 = new Task(1, "Test1", "Desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test2", "Desc2", TaskStatus.NEW);
        FileBackedTaskManager manager = createTaskManager();
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getTask(task1.getId()).getId());
        assertEquals(2, loadedManager.getTask(task2.getId()).getId());
    }

    @Test
    void shouldUpdateIdGeneratorAfterLoading() throws IOException {
        FileBackedTaskManager manager = createTaskManager();

        Task task1 = new Task(1, "Test1", "Desc1", TaskStatus.NEW);
        Task task2 = new Task(2, "Test2", "Desc2", TaskStatus.NEW);
        manager.createTask(task1);
        manager.createTask(task2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task task3 = new Task(1,"Test3", "Desc3", TaskStatus.NEW);
        loadedManager.createTask(task3);
        assertEquals(3, loadedManager.getTask(task3.getId()).getId());
    }

    @Test
    void shouldSaveAndLoadTaskWithTimeFields() throws IOException {
        FileBackedTaskManager manager = createTaskManager();

        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);
        Task task = new Task(1, "Test", "Desc", TaskStatus.NEW, duration, startTime);

        manager.createTask(task);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask = loadedManager.getTask(1);
        assertEquals(duration, loadedTask.getDuration());
        assertEquals(startTime, loadedTask.getStartTime());
    }
}
