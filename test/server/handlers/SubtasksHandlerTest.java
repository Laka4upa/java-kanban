package server.handlers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import tasks.Epic;
import tasks.Subtask;

class SubtasksHandlerTest extends BaseHttpHandlerTest {

    @Test
    void testGetAllSubtasks_returnsList() throws Exception {
        Epic epic = createTestEpic("Parent");
        createTestSubtask("One", epic.getId());
        createTestSubtask("Two", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
    }

    @Test
    void testGetSubtaskById_success() throws Exception {
        Epic epic = createTestEpic("Parent");
        Subtask subtask = createTestSubtask("Child", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask result = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), result.getId());
    }

    @Test
    void testGetSubtaskById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testCreateSubtask_success() throws Exception {
        Epic epic = createTestEpic("Epic");
        Subtask subtask = new Subtask("New Sub", "Desc", epic.getId());
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Subtask created = gson.fromJson(response.body(), Subtask.class);
        assertEquals(epic.getId(), created.getEpicId());
    }

    @Test
    void testUpdateSubtask_success() throws Exception {
        Epic epic = createTestEpic("Epic");
        Subtask subtask = createTestSubtask("ToUpdate", epic.getId());
        subtask.setName("Updated Name");

        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("Updated Name", taskManager.getSubtaskById(subtask.getId()).getName());
    }

    @Test
    void testCreateSubtask_withInvalidEpic_returns400() throws Exception {
        Subtask subtask = new Subtask("Invalid", "Desc", 999); // несуществующий эпик
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("эпик не существует"));
    }

    @Test
    void testDeleteSubtaskById_success() throws Exception {
        Epic epic = createTestEpic("Epic");
        Subtask subtask = createTestSubtask("ToDelete", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testDeleteSubtaskById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/999"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testDeleteAllSubtasks_success() throws Exception {
        Epic epic = createTestEpic("Epic");
        createTestSubtask("Sub1", epic.getId());
        createTestSubtask("Sub2", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }
}