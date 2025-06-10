package server.handlers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import util.Status;

class EpicsHandlerTest extends BaseHttpHandlerTest {

    @Test
    void testCreateEpic() throws Exception {
        Epic epic = new Epic("Test Epic", "Description");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем что эпик добавился
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        Epic[] epics = gson.fromJson(getResponse.body(), Epic[].class);
        assertEquals(1, epics.length);
        assertEquals("Test Epic", epics[0].getName());
        assertEquals(Status.NEW, epics[0].getStatus());
    }

    @Test
    void testGetEpicSubtasks() throws Exception {
        Epic epic = createTestEpic("Parent Epic");
        createTestSubtask("Test Subtask", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(1, subtasks.length);
        assertEquals("Test Subtask", subtasks[0].getName());
    }

    @Test
    void testCreateEpic_success() throws Exception {
        Epic epic = new Epic("Test", "Desc");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        Epic created = gson.fromJson(response.body(), Epic.class);
        assertNotNull(created.getId());
        assertEquals(Status.NEW, created.getStatus());
    }

    @Test
    void testUpdateEpicStatus_autoCalculated() throws Exception {
        // Создаем эпик и подзадачу
        Epic epic = createTestEpic("Test Epic");
        Subtask subtask = createTestSubtask("Test Subtask", epic.getId());

        // Меняем статус подзадачи на IN_PROGRESS
        subtask.setStatus(Status.IN_PROGRESS);
        String subtaskJson = gson.toJson(subtask);

        // Обновляем подзадачу
        HttpRequest updateSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/subtasks/" + subtask.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        client.send(updateSubtaskRequest, HttpResponse.BodyHandlers.ofString());

        // Проверяем, что статус эпика автоматически обновился
        HttpRequest getEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getEpicRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic updatedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void testUpdateEpic_ignoresManualStatusChange() throws Exception {
        Epic epic = createTestEpic("Test");
        epic.setStatus(Status.IN_PROGRESS);  // вручную, но это игнорируется
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(Status.NEW, taskManager.getEpicById(epic.getId()).getStatus()); // ничего не поменялось
    }

    @Test
    void testDeleteEpic_success() throws Exception {
        Epic epic = createTestEpic("Test");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testDeleteEpic_withSubtasks() throws Exception {
        Epic epic = createTestEpic("Test");
        createTestSubtask("Subtask", epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics/" + epic.getId()))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpicById(epic.getId()));
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testDeleteAllEpics_success() throws Exception {
        createTestEpic("Test1");
        createTestEpic("Test2");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/epics"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }
}
