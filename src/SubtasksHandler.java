import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final InMemoryTaskManager taskManager;

    public SubtasksHandler(InMemoryTaskManager taskManager) {
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
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            String response = GSON.toJson(subtasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);
            Subtask subtask = taskManager.getSubtask(id);

            if (subtask != null) {
                String response = GSON.toJson(subtask);
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
        System.out.println("Received subtask JSON: " + requestBody);

        try {
            Subtask subtask = GSON.fromJson(requestBody, Subtask.class);

            if (subtask == null) {
                sendNotFound(exchange);
                return;
            }

            if (subtask.getId() == 0) {
                Subtask createdSubtask = taskManager.createSubtask(subtask);
                if (createdSubtask != null) {
                    String response = GSON.toJson(createdSubtask);
                    sendCreated(exchange, response);
                    System.out.println("Created subtask with ID: " + createdSubtask.getId());
                } else {
                    sendNotFound(exchange);
                }
            } else {
                // Обновление
                Subtask existingSubtask = taskManager.getSubtask(subtask.getId());
                if (existingSubtask != null) {
                    taskManager.updateSubtask(subtask);
                    sendSuccess(exchange, "{\"message\": \"Subtask updated successfully\"}");
                    System.out.println("Updated subtask with ID: " + subtask.getId());
                } else {
                    sendNotFound(exchange);
                }
            }
        } catch (TimeConflictException e) {
            System.err.println("Time conflict error: " + e.getMessage());
            sendHasOverlaps(exchange);
        } catch (Exception e) {
            System.err.println("Error in handlePost: " + e.getMessage());
            e.printStackTrace();
            handleException(exchange, e);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Subtask subtask = taskManager.getSubtask(id);
            if (subtask != null) {
                taskManager.deleteSubtask(id);
                sendSuccess(exchange, "{\"message\": \"Subtask deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/subtasks")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "{\"message\": \"All subtasks deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}