import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final InMemoryTaskManager taskManager;

    public TasksHandler(InMemoryTaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            handleException(exchange, e);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            String response = GSON.toJson(tasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);
            Task task = taskManager.getTask(id);

            if (task != null) {
                String response = GSON.toJson(task);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        System.out.println("Received JSON: " + requestBody); // Лог для отладки

        try {
            Task task = GSON.fromJson(requestBody, Task.class);

            if (task == null) {
                sendNotFound(exchange);
                return;
            }

            if (task.getId() == 0) {
                Task createdTask = taskManager.createTask(task);
                if (createdTask != null) {
                    String response = GSON.toJson(createdTask);
                    sendCreated(exchange, response);
                    System.out.println("Created task with ID: " + createdTask.getId());
                } else {
                    sendNotFound(exchange);
                }
            } else {
                // Обновление существующей задачи если указан айди
                Task existingTask = taskManager.getTask(task.getId());
                if (existingTask != null) {
                    taskManager.updateTask(task);
                    sendCreated(exchange, "{\"message\": \"Task updated successfully\"}");
                    System.out.println("Updated task with ID: " + task.getId());
                } else {
                    sendNotFound(exchange);
                }
            }
        } catch (TimeConflictException e) {
            System.out.println(e.getMessage());
            sendHasOverlaps(exchange);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            handleException(exchange, e);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTask(id);
            if (task != null) {
                taskManager.deleteTask(id);
                sendSuccess(exchange, "{\"message\": \"Task deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/tasks")) {
            taskManager.deleteAllTasks();
            sendSuccess(exchange, "{\"message\": \"All tasks deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}
