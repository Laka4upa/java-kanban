package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            List<Task> history = taskManager.getHistory();
            sendSuccess(exchange, gson.toJson(history));

        } catch (Exception e) {
            sendServerError(exchange, e);
        }
    }
}