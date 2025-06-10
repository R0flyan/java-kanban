import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {

    private int newId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    // удаление всех эпиков с подзадачами
    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory(); // Делегируем вызов HistoryManager
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) return null;
        int id = createId();
        task.setId(id);
        tasks.put(task.getId(), task);
        return tasks.get(id);
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;
        int id = createId();
        epic.setId(id);
        epics.put(epic.getId(), epic);
        return epics.get(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) return null;
        int id = createId();
        subtask.setId(id);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return null;

        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        return subtasks.get(id);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;

        Subtask savedSubtask = subtasks.get(subtask.getId());
        if (savedSubtask.getEpicId() != subtask.getEpicId()) {
            // если эпик изменился, то нужно обновить списки в старом и новом эпиках
            Epic oldEpic = epics.get(savedSubtask.getEpicId());
            if (oldEpic != null) {
                oldEpic.removeSubtaskId(subtask.getId());
                updateEpicStatus(oldEpic.getId());
            }

            Epic newEpic = epics.get(subtask.getEpicId());
            if (newEpic != null) {
                newEpic.addSubtaskId(subtask.getId());
                updateEpicStatus(newEpic.getId());
            }
        }

        savedSubtask.setName(subtask.getName());
        savedSubtask.setDescription(subtask.getDescription());
        savedSubtask.setStatus(subtask.getStatus());
        savedSubtask.setEpicId(subtask.getEpicId());

        updateEpicStatus(savedSubtask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    // удаление эпика с подзадачами по айди
    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) return;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic.getId());
        }
        subtasks.remove(id);
    }

    // получение всех подзадач эпика
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return new ArrayList<>();

        List<Subtask> result = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private int createId() {
        return newId++;
    }
}
