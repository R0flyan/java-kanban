import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TaskManager {
    private int newId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    private int createId() {
        return newId++;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    // удаление всех эпиков с подзадачами
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public Task createTask(Task task) {
        if (task == null) return null;

        int id = createId();
        Task newTask = new Task(id, task.getName(), task.getDescription(), task.getStatus());
        tasks.put(id, newTask);
        return newTask;
    }

    public Epic createEpic(Epic epic) {
        if (epic == null) return null;

        int id = createId();
        Epic newEpic = new Epic(id, epic.getName(), epic.getDescription());
        epics.put(id, newEpic);
        return newEpic;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) return null;

        int id = createId();
        Subtask newSubtask = new Subtask(id, subtask.getName(), subtask.getDescription(),
                subtask.getStatus(), subtask.getEpicId());

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return null;

        subtasks.put(id, newSubtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        return newSubtask;
    }

    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

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

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    // удаление эпика с подзадачами по айди
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) return;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

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
}
