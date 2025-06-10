package server.handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import tasks.Task;
import util.Status;

class TasksHandlerTest extends BaseHttpHandlerTest {

    @Test
    void testCreateTask_success() throws Exception {
        Task task = new Task("Test", "Desc", Status.NEW);
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Task created = gson.fromJson(response.body(), Task.class);
        assertNotNull(created.getId());
    }

    @Test
    void testCreateTask_invalidJson_failure() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString("{invalid}"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
    }

    @Test
    void testGetTaskById_success() throws Exception {
        Task task = createTestTask("Test", Status.NEW);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/" + task.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task result = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), result.getId());
    }

    @Test
    void testGetTaskById_notFound() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testUpdateTask_success() throws Exception {
        Task task = createTestTask("Test", Status.NEW);
        task.setStatus(Status.IN_PROGRESS);
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/" + task.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task updated = gson.fromJson(response.body(), Task.class);
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void testDeleteTask_success() throws Exception {
        Task task = createTestTask("Test", Status.NEW);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks/" + task.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void testDeleteAllTasks_success() throws Exception {
        createTestTask("Test1", Status.NEW);
        createTestTask("Test2", Status.NEW);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/tasks"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, taskManager.getAllTasks().size());
    }
}