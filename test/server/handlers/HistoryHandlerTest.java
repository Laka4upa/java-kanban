package server.handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import tasks.Task;
import util.Status;

class HistoryHandlerTest extends BaseHttpHandlerTest {

    @Test
    void testGetHistory_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void testGetHistory_withTasks() throws Exception {
        Task task1 = createTestTask("Task 1", Status.NEW);
        Task task2 = createTestTask("Task 2", Status.NEW);

        // Добавляем в историю
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/history"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length);
    }
}