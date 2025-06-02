package managers;

import org.junit.jupiter.api.*;
import tasks.*;
import util.Status;

import java.io.IOException;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = createTaskManager();
    }

    protected abstract T createTaskManager();

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Test Task", "Description");
        taskManager.addTask(task);
        Task savedTask = taskManager.getTaskById(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldAddAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);
        Epic savedEpic = taskManager.getEpicById(epic.getId());

        assertNotNull(savedEpic, "Эпик не найден");
        assertEquals(epic, savedEpic, "Эпики не совпадают");
    }

    @Test
    void shouldAddAndGetSubtask() {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        taskManager.addSubtask(subtask);

        Subtask savedSubtask = taskManager.getSubtaskById(subtask.getId());
        assertNotNull(savedSubtask, "Подзадача не найдена");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "Неверный EpicId у подзадачи");
    }

    @Test
    void shouldUpdateEpicStatus() {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);

        // a. Все подзадачи NEW
        Subtask subtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        subtask1.setStatus(Status.NEW);
        taskManager.addSubtask(subtask1);

        assertEquals(Status.NEW, epic.getStatus(), "Статус должен быть NEW");

        // b. Все подзадачи DONE
        Subtask updatedSubtask1 = new Subtask("Subtask 1", "Desc", epic.getId());
        updatedSubtask1.setId(subtask1.getId());  // Важно сохранить тот же ID!
        updatedSubtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(updatedSubtask1);  // Передаём новую версию

        Epic updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getStatus(), "Статус должен быть DONE");

        // c. NEW и DONE
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", epic.getId());
        subtask2.setStatus(Status.NEW);
        taskManager.addSubtask(subtask2);

        updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus(), "Статус должен быть IN_PROGRESS");

        // d. Подзадачи IN_PROGRESS
        Subtask updatedSubtask1InProgress = new Subtask("Subtask 1", "Desc", epic.getId());
        updatedSubtask1InProgress.setId(subtask1.getId());
        updatedSubtask1InProgress.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(updatedSubtask1InProgress);

        updatedEpic = taskManager.getEpicById(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getStatus(), "Статус должен быть IN_PROGRESS");
    }

    @Test
    void shouldDetectTimeOverlaps() {
        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(2));
        taskManager.addTask(task1);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(task1.getStartTime().plusHours(1));
        task2.setDuration(Duration.ofHours(1));

        assertTrue(taskManager.hasTimeOverlap(task2), "Должно быть пересечение");

        Task task3 = new Task("Task 3", "Description");
        task3.setStartTime(task1.getStartTime().plusHours(3));
        task3.setDuration(Duration.ofHours(1));

        assertFalse(taskManager.hasTimeOverlap(task3), "Не должно быть пересечения");
    }
}
