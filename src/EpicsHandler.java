import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final InMemoryTaskManager taskManager;

    public EpicsHandler(InMemoryTaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            String response = GSON.toJson(epics);
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);
            Epic epic = taskManager.getEpic(id);

            if (epic != null) {
                String response = GSON.toJson(epic);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            // Получение подзадачи эпика по айди эпика
            String[] pathParts = path.split("/");
            int epicId = Integer.parseInt(pathParts[2]);
            Epic epic = taskManager.getEpic(epicId);

            if (epic != null) {
                List<Subtask> epicSubtasks = taskManager.getEpicSubtasks(epicId);
                String response = GSON.toJson(epicSubtasks);
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
            Epic epic = GSON.fromJson(requestBody, Epic.class);
            if (epic == null) {
                sendNotFound(exchange);
                return;
            }

            if (epic.getId() == 0) {
                Epic createdEpic = taskManager.createEpic(epic);
                if (createdEpic != null) {
                    String response = GSON.toJson(createdEpic);
                    sendCreated(exchange, response);
                    System.out.println("Created epic with ID: " + createdEpic.getId());
                } else {
                    sendNotFound(exchange);
                }
            } else {
                // Обновление
                Epic existingEpic = taskManager.getEpic(epic.getId());
                if (existingEpic != null) {
                    taskManager.updateEpic(epic);
                    sendSuccess(exchange, "{\"message\": \"Subtask updated successfully\"}");
                    System.out.println("Updated subtask with ID: " + epic.getId());
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
        if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Epic epic = taskManager.getEpic(id);
            if (epic != null) {
                taskManager.deleteEpic(id);
                sendSuccess(exchange, "{\"message\": \"Epic deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "{\"message\": \"All epics deleted successfully\"}");
        } else {
            sendNotFound(exchange);
        }
    }
}
