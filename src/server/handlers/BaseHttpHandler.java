package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import managers.TaskManager;
import util.DurationAdapter;
import util.LocalDateTimeAdapter;

public abstract class BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // Основной метод отправки текстового ответа
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    // Успешные ответы
    protected void sendSuccess(HttpExchange exchange, String responseData) throws IOException {
        sendText(exchange, responseData, 200);
    }

    protected void sendCreated(HttpExchange exchange, String responseData) throws IOException {
        sendText(exchange, responseData, 201);
    }

    // Ответы об ошибках
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"message\":\"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"message\":\"Time conflict with existing tasks\"}", 406);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\":\"" + escapeJson(message) + "\"}", 400);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Method Not Allowed\"}", 405);
    }

    protected void sendServerError(HttpExchange exchange, Exception e) throws IOException {
        sendText(exchange, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}", 500);
    }

    // Вспомогательные методы
    protected String readRequest(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected int extractIdFromPath(String path) throws NumberFormatException {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
