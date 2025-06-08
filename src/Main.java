public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = new TaskManager();
        Task task1 = manager.createTask(new Task(0, "taskOne",
                "Description of taskOne", TaskStatus.NEW));
        Task task2 = manager.createTask(new Task(0, "taskTwo",
                "Description of taskTwo", TaskStatus.NEW));

        Epic epic1 = manager.createEpic(new Epic(0, "Epic 1", "Epic description"));

        Subtask epic1Subtask1 = manager.createSubtask(new Subtask(0, "e1Subtask 1",
                "e1Sub1 description", TaskStatus.NEW, epic1.getId()));
        Subtask epic1Subtask2 = manager.createSubtask(new Subtask(1, "e1Subtask 2",
                "e1Sub2 description", TaskStatus.NEW, epic1.getId()));

        Epic epic2 = manager.createEpic(new Epic(0, "Epic 2", "Epic description"));

        Subtask epic2Subtask1 = manager.createSubtask(new Subtask(0, "e2Subtask 1",
                "e2Sub1 description", TaskStatus.NEW, epic2.getId()));

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());

        epic1Subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(epic1Subtask1);
        System.out.println(epic1.getStatus());

        epic1Subtask1.setStatus(TaskStatus.DONE);
        epic1Subtask2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(epic1Subtask1);
        manager.updateSubtask(epic1Subtask2);
        manager.createEpic(epic1);
        System.out.println(epic1.getStatus());

        manager.deleteTask(1);
        System.out.println(manager.getAllTasks());

    }
}
