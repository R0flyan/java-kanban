import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static CSVFormatter formatter;
    private final File file;

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
        this.formatter = new CSVFormatter();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return super.getPrioritizedTasks();
    }

    // Загрузки из файла
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file, Managers.getDefaultHistory());

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = content.split(System.lineSeparator());

            // Загружаем все эпики
            for (int i = 1; i < lines.length; i++) {
                Task task = CSVFormatter.fromString(lines[i]);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                }
            }
            // Подзадачи
            for (int i = 1; i < lines.length; i++) {
                Task task = formatter.fromString(lines[i]);
                if (task instanceof Subtask) {
                    Subtask subtask = (Subtask) task;
                    manager.subtasks.put(subtask.getId(), subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtaskId(subtask.getId());
                    }
                }
            }
            // Обычные задачи
            for (int i = 1; i < lines.length; i++) {
                Task task = formatter.fromString(lines[i]);
                if (task instanceof Task && !(task instanceof Epic) && !(task instanceof Subtask)) {
                    manager.tasks.put(task.getId(), task);
                }
            }

            for (Epic epic : manager.epics.values()) {
                List<Subtask> epicSubtasks = manager.getEpicSubtasks(epic.getId());
                epic.calculateTimes(epicSubtasks);
            }

            manager.newId = manager.getMaxId() + 1;

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }

        for (Task task : manager.tasks.values()) {
            manager.addToPriority(task);
        }

        for (Subtask subtask : manager.subtasks.values()) {
            manager.addToPriority(subtask);
        }

        return manager;
    }

    // Переопределение модифицирующих операций для сохранения состояний после изменений
    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    // Метод сохранения состояния в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,duration,startTime,endTime");
            writer.newLine();
            for (Task task : getAllTasks()) {
                writer.write(formatter.toString(task));
                writer.newLine();
            }

            for (Task task : getAllEpics()) {
                writer.write(formatter.toString(task));
                writer.newLine();
            }

            for (Task task : getAllSubtasks()) {
                writer.write(formatter.toString(task));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    private int getMaxId() {
        int max = 0;
        for (int id : tasks.keySet()) {
            if (id > max) {
                max = id;
            }
        }
        for (int id : epics.keySet()) {
            if (id > max) {
                max = id;
            }
        }
        for (int id : subtasks.keySet()) {
            if (id > max) {
                max = id;
            }
        }
        return max;
    }
}
