package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import managers.TaskManager;
import tasks.Task;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;


public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
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
                    handlePost(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendMethodNotAllowed(exchange);
            }
        } catch (Exception e) {
            sendServerError(exchange, e);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            sendSuccess(exchange, gson.toJson(tasks));
        } else {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task != null) {
                    sendSuccess(exchange, gson.toJson(task));
                } else {
                    sendNotFound(exchange);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный формат ID задачи");
            }
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        try {
            String requestBody = readRequest(exchange);
            Task task = gson.fromJson(requestBody, Task.class);

            if (task == null) {
                sendBadRequest(exchange, "Тело запроса не может быть пустым");
                return;
            }

            if (path.equals("/tasks")) {
                if (task.getId() != 0) {
                    sendBadRequest(exchange, "Для создания задачи ID должен быть 0");
                    return;
                }
                taskManager.addTask(task);
                sendCreated(exchange, gson.toJson(task));
            } else {
                try {
                    int pathId = extractIdFromPath(path);
                    if (task.getId() != pathId) {
                        sendBadRequest(exchange, "ID в пути и теле запроса не совпадают");
                        return;
                    }
                    taskManager.updateTask(task);
                    sendSuccess(exchange, gson.toJson(task));
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, "Некорректный формат ID задачи");
                }
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            taskManager.removeAllTasks();
            sendSuccess(exchange, "Все задачи успешно удалены");
        } else {
            try {
                int id = extractIdFromPath(path);
                taskManager.removeTaskById(id);
                sendSuccess(exchange, "Задача с ID=" + id + " удалена");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный формат ID задачи");
            }
        }
    }
}