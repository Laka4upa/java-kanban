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
import tasks.Subtask;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();;
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
        if (path.equals("/subtasks")) {
            List<Subtask> subtasks = taskManager.getAllSubtasks();
            sendSuccess(exchange, gson.toJson(subtasks));
        } else if (path.startsWith("/subtasks/")) {
            handleGetSubtaskById(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetSubtaskById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask != null) {
                sendSuccess(exchange, gson.toJson(subtask));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный формат ID подзадачи");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        try {
            String requestBody = readRequest(exchange);
            Subtask subtask = gson.fromJson(requestBody, Subtask.class);

            if (subtask == null) {
                sendBadRequest(exchange, "Тело запроса не может быть пустым");
                return;
            }

            if (path.equals("/subtasks")) {
                if (subtask.getId() != 0) {
                    sendBadRequest(exchange, "Для создания подзадачи ID должен быть 0");
                    return;
                }
                if (!epicExists(subtask.getEpicId())) {
                    sendBadRequest(exchange, "Указанный эпик не существует");
                    return;
                }
                taskManager.addSubtask(subtask);
                sendCreated(exchange, gson.toJson(subtask));
            } else if (path.startsWith("/subtasks/")) {
                try {
                    int pathId = extractIdFromPath(path);
                    if (subtask.getId() != pathId) {
                        sendBadRequest(exchange, "ID в пути и теле запроса не совпадают");
                        return;
                    }
                    if (!epicExists(subtask.getEpicId())) {
                        sendBadRequest(exchange, "Указанный эпик не существует");
                        return;
                    }
                    taskManager.updateSubtask(subtask);
                    sendSuccess(exchange, gson.toJson(subtask));
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, "Некорректный формат ID подзадачи");
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/subtasks")) {
            taskManager.removeAllSubtasks();
            sendSuccess(exchange, "Все подзадачи успешно удалены");
        } else if (path.startsWith("/subtasks/")) {
            try {
                int id = extractIdFromPath(path);
                Subtask existing = taskManager.getSubtaskById(id);
                if (existing == null) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.removeSubtaskById(id);
                sendSuccess(exchange, "Подзадача с ID=" + id + " удалена");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный формат ID подзадачи");
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private boolean epicExists(int epicId) {
        return taskManager.getEpicById(epicId) != null;
    }
}