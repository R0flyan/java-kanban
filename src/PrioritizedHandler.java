import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final InMemoryTaskManager taskManager;

    public PrioritizedHandler(InMemoryTaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            String response = GSON.toJson(prioritizedTasks);
            sendSuccess(exchange, response);
        }
    }
}
