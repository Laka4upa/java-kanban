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
import tasks.Epic;
import tasks.Subtask;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            sendSuccess(exchange, gson.toJson(epics));
        } else if (path.startsWith("/epics/") && path.endsWith("/subtasks")) {
            handleGetEpicSubtasks(exchange, path);
        } else if (path.startsWith("/epics/")) {
            handleGetEpicById(exchange, path);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path.replace("/subtasks", ""));
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                sendSuccess(exchange, gson.toJson(epic));
            } else {
                sendNotFound(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный формат ID эпика");
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String path) throws IOException {
        try {
            int epicId = extractIdFromPath(path.replace("/subtasks", ""));
            Epic epic = taskManager.getEpicById(epicId);
            if (epic == null) {
                sendNotFound(exchange);
                return;
            }
            List<Subtask> subtasks = taskManager.getAllSubtasksOfEpic(epic);
            sendSuccess(exchange, gson.toJson(subtasks));
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Некорректный формат ID эпика");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        try {
            String requestBody = readRequest(exchange);
            Epic epic = gson.fromJson(requestBody, Epic.class);

            if (epic == null) {
                sendBadRequest(exchange, "Тело запроса не может быть пустым");
                return;
            }

            if (path.equals("/epics")) {
                if (epic.getId() != 0) {
                    sendBadRequest(exchange, "Для создания эпика ID должен быть 0");
                    return;
                }
                taskManager.addEpic(epic);
                sendCreated(exchange, gson.toJson(epic));
            } else if (path.startsWith("/epics/")) {
                try {
                    int pathId = extractIdFromPath(path);
                    if (epic.getId() != pathId) {
                        sendBadRequest(exchange, "ID в пути и теле запроса не совпадают");
                        return;
                    }
                    taskManager.updateEpic(epic);
                    sendSuccess(exchange, gson.toJson(epic));
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, "Некорректный формат ID эпика");
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/epics")) {
            taskManager.removeAllEpics();
            sendSuccess(exchange, "Все эпики успешно удалены");
        } else if (path.startsWith("/epics/")) {
            try {
                int id = extractIdFromPath(path);
                taskManager.removeEpicById(id);
                sendSuccess(exchange, "Эпик с ID=" + id + " удален");
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "Некорректный формат ID эпика");
            }
        } else {
            sendNotFound(exchange);
        }
    }
}