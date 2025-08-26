import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTest {
    private HttpTaskServer taskServer;
    private InMemoryTaskManager manager;
    private HttpClient client;
    private Gson gson;

    public HttpTaskManagerTest() throws IOException {

    }

    @BeforeEach
    void setUp() throws IOException {
        taskServer = new HttpTaskServer();
        manager = taskServer.taskManager;
        taskServer.start();
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    @AfterEach
    void shutDown() {
        if (taskServer != null) {
            taskServer.stop();
        }
    }

    @Test
    void createTaskTest() throws IOException, InterruptedException {
        Task task = new Task(0, "task", "description", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.now());

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
        Task createdTask = manager.getAllTasks().get(0);
        assertEquals("task", createdTask.getName());
        assertEquals("description", createdTask.getDescription());
    }

    @Test
    void createEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "epic", "epicDescription");

        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllEpics().size());
        Epic createdEpic = manager.getAllEpics().get(0);
        assertEquals("epic", createdEpic.getName());
        assertEquals("epicDescription", createdEpic.getDescription());
    }

    @Test
    void getTaskByIdTest() throws IOException, InterruptedException {
        Task task = new Task(0, "task", "description", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.now().plusHours(1));

        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        Task createdTask = manager.getAllTasks().get(0);
        int taskId = createdTask.getId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode());

        Task taskFromResponse = gson.fromJson(getResponse.body(), Task.class);

        assertEquals(taskId, taskFromResponse.getId());
        assertEquals("task", taskFromResponse.getName());
        assertEquals("description", taskFromResponse.getDescription());
    }

    @Test
    void updateTaskTest() throws IOException, InterruptedException {
        // Создаем задачу
        Task originalTask = new Task(0, "Original", "Original desc", TaskStatus.NEW,
                Duration.ofMinutes(60), LocalDateTime.now());

        String originalJson = gson.toJson(originalTask);
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(originalJson))
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());


        Task createdTask = manager.getAllTasks().get(0);
        int taskId = createdTask.getId();
        // Обновляем задачу
        Task updatedTask = new Task(taskId, "Updated", "Updated desc", TaskStatus.DONE,
                Duration.ofMinutes(120), LocalDateTime.now().plusHours(2));

        String updatedJson = gson.toJson(updatedTask);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        Task finalTask = manager.getAllTasks().get(0);
        assertEquals("Updated", finalTask.getName());
        assertEquals("Updated desc", finalTask.getDescription());
        assertEquals(TaskStatus.DONE, finalTask.getStatus());
        assertEquals(Duration.ofMinutes(120), finalTask.getDuration());
    }
}
