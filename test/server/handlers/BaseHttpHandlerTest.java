package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import server.HttpTaskServer;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;
import util.Status;

public class BaseHttpHandlerTest {
    protected TaskManager taskManager;
    protected HttpTaskServer taskServer;
    protected HttpClient client;
    protected Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskManager.removeAllTasks();
        taskManager.removeAllSubtasks();
        taskManager.removeAllEpics();
        taskServer.start();
        client = HttpClient.newHttpClient();
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    protected String getBaseUrl() {
        return "http://localhost:8080";
    }

    protected Task createTestTask(String name, Status status) {
        Task task = new Task(name, "Description", status);
        taskManager.addTask(task);
        return task;
    }

    protected Epic createTestEpic(String name) {
        Epic epic = new Epic(name, "Description");
        taskManager.addEpic(epic);
        return epic;
    }

    protected Subtask createTestSubtask(String name, int epicId) {
        Subtask subtask = new Subtask(name, "Description", epicId);
        taskManager.addSubtask(subtask);
        return subtask;
    }
}