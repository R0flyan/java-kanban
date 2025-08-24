import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int newId = 1;
    final Map<Integer, Task> tasks = new HashMap<>();
    final Map<Integer, Epic> epics = new HashMap<>();
    final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            (t1, t2) -> {
                if (t1.getStartTime() == null && t2.getStartTime() == null) return 0;
                if (t1.getStartTime() == null) return 1;
                if (t2.getStartTime() == null) return -1;
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
    );

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
        if (hasTimeOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей");
        }
        int id = createId();
        task.setId(id);
        tasks.put(task.getId(), task);
        addToPriority(task);
        return tasks.get(id);
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) return null;
        int id = createId();
        epic.setId(id);
        if (epic.getSubtaskIds() == null) {
            epic.setSubtaskIds(new ArrayList<>());
        }
        epics.put(epic.getId(), epic);
        return epics.get(id);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) return null;
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени с существующей");
        }
        int id = createId();
        subtask.setId(id);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return null;

        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(id);
        updateEpicStatus(epic.getId());
        addToPriority(subtask);
        return subtasks.get(id);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        if (hasTimeOverlap(task)) {
            throw new TimeConflictException("Обновленная задача пересекается по времени с существующей");
        }
        Task savedTask = tasks.get(task.getId());
        removeFromPriority(savedTask);
        savedTask.setName(task.getName());
        savedTask.setDescription(task.getDescription());
        savedTask.setStatus(task.getStatus());
        tasks.put(task.getId(), task);
        addToPriority(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) return;

        Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
        if (epic.getSubtaskIds() == null) {
            epic.setSubtaskIds(savedEpic.getSubtaskIds());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) return;
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Обновленная подзадача пересекается по времени с существующей");
        }

        Subtask savedSubtask = subtasks.get(subtask.getId());
        removeFromPriority(savedSubtask);
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

        addToPriority(subtask);
        updateEpicStatus(savedSubtask.getEpicId());
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPriority(task);
        }
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
        removeFromPriority(subtask);
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public boolean hasTimeOverlap(Task newTask) {
        if (newTask == null || newTask.getStartTime() == null) {
            return false;
        }

        return getAllTasks().stream()
                .filter(task -> task.getId() != newTask.getId()) // Исключаем саму себя
                .filter(task -> task.getStartTime() != null)
                .anyMatch(existingTask -> TimeOverlapping.isTimeOverlapping(existingTask, newTask));
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

    protected void addToPriority(Task task) {
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void removeFromPriority(Task task) {
        prioritizedTasks.remove(task);
    }
}
