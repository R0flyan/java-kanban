import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task(0, "Task1", "Description", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task(0, "Task2", "Description", TaskStatus.NEW);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }
}