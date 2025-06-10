import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {
    @Test
    void getDefaultShouldReturnInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач должен инициализироваться");
    }

}