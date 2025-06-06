package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import managers.TaskManager;
import tasks.Task;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
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