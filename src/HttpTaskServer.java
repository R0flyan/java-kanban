import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer server;
    protected InMemoryTaskManager taskManager;


    public HttpTaskServer() throws IOException {
        this.taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчики
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("Starting server on port " + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("Server stopped");
    }

    public static void main(String[] args) throws IOException {

        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();

        //для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(taskServer::stop));
    }
}
