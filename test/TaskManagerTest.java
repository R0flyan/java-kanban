import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected abstract T createTaskManager() throws IOException;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    @Test
    void epicStatusShouldBeNewWhenNoSubtasks() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.createEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeNewWhenAllSubtasksNew() {
        Epic epic = taskManager.createEpic(new Epic(1, "Epic", "Description"));
        Subtask subtask1 = new Subtask(0,"Sub1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(0,"Sub2", "Desc", TaskStatus.NEW, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneWhenAllSubtasksDone() {
        Epic epic = taskManager.createEpic(new Epic(1,"Epic", "Description"));
        Subtask subtask1 = new Subtask(0,"Sub1", "Desc", TaskStatus.DONE, epic.getId());
        Subtask subtask2 = new Subtask(0,"Sub2", "Desc", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressWhenSubtasksNewAndDone() {
        Epic epic = taskManager.createEpic(new Epic(1,"Epic", "Description"));
        Subtask subtask1 = new Subtask(0,"Sub1", "Desc", TaskStatus.NEW, epic.getId());
        Subtask subtask2 = new Subtask(0,"Sub2", "Desc", TaskStatus.DONE, epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldDetectTimeOverlap() {
        Task task1 = new Task(1,"Task1", "Desc", TaskStatus.NEW,Duration.ofHours(2),
                LocalDateTime.now());
        taskManager.createTask(task1);

        Task task2 = new Task(2, "Task2", "Desc", TaskStatus.NEW, Duration.ofHours(2),
                LocalDateTime.now().plusHours(1));

        assertThrows(TimeConflictException.class, () -> taskManager.createTask(task2));
    }

    @Test
    void shouldNotDetectTimeOverlapForTasksWithoutTime() {
        Task task1 = new Task(1, "Task1", "Desc", TaskStatus.NEW);
        taskManager.createTask(task1);

        Task task2 = new Task(2, "Task2", "Desc", TaskStatus.NEW);
        assertDoesNotThrow(() -> taskManager.createTask(task2));
    }
}
