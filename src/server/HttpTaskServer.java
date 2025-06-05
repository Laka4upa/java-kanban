package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;
import util.Managers;
import managers.TaskManager;
import server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HTTP-сервер, обрабатывающий REST-запросы к TaskManager.
 */
public class HttpTaskServer {
    private static final int PORT = 8080;

    private final HttpServer server;
    private final TaskManager taskManager;
    private final Gson gson;

    public HttpTaskServer() throws IOException {
        this.taskManager = Managers.getDefault();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupContexts();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new Gson();
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupContexts();
    }

    private void setupContexts() {
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedTasksHandler(taskManager));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен.");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            System.out.println("Не удалось запустить сервер: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
