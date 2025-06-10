import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic(0, "Epic1", "Description");
        epic1.setId(1);

        Epic epic2 = new Epic(0, "Epic2", "Description");
        epic2.setId(1);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
    }

    @Test
    void epicShouldNotBeAsSubtask() {
        Epic epic = new Epic(0, "Epic1", "Description");
        epic.setId(1);
        epic.addSubtaskId(epic.getId());
        assertFalse(epic.getSubtaskIds().contains(epic.getId()));
    }
}