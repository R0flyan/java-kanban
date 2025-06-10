import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(0, "Subtask1", "Description", TaskStatus.NEW, 1);
        subtask1.setId(1);

        Subtask subtask2 = new Subtask(0, "Subtask2", "Description", TaskStatus.NEW, 1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test
    void subtaskShouldNotBeItsOwnEpic() {
        Subtask subtask = new Subtask(0, "Subtask", "Description", TaskStatus.NEW, 2);
        subtask.setId(1);
        subtask.setEpicId(1);
        assertNotEquals(subtask.getId(), subtask.getEpicId(), "ID подзадачи не должно совпадать с ID её эпика");
    }
}