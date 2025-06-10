package server.handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tasks.Task;
import util.Status;

class PrioritizedTasksHandlerTest extends BaseHttpHandlerTest {

    @Test
    void testGetPrioritizedTasks() throws Exception {
        Task task1 = new Task("Task 1", "Desc", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Desc", Status.NEW,
                Duration.ofMinutes(15), LocalDateTime.now());

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/prioritized"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals("Task 2", prioritized[0].getName()); // Должен быть первым
    }
}