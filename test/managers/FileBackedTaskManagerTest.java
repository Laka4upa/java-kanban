package managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import tasks.*;
import exceptions.*;
import util.Status;

import java.io.*;
import java.time.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        super.setUp();  // Инициализация taskManager через createTaskManager()
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            assertTrue(tempFile.delete());
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(tempFile);
    }

    @Test
    void shouldSaveAndLoadEmptyManager() {
        taskManager.save(); // Сохраняем пустой менеджер

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertTrue(loadedManager.getAllTasks().isEmpty()),
                () -> assertTrue(loadedManager.getAllEpics().isEmpty()),
                () -> assertTrue(loadedManager.getAllSubtasks().isEmpty())
        );
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Desc", epic.getId());
        taskManager.addSubtask(subtask);

        Task task = new Task("Test Task", "Description");
        taskManager.addTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size()),
                () -> assertEquals(1, loadedManager.getAllEpics().size()),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size())
        );
    }

    //Проверка пересечения времени у подзадач одного эпика
    @Test
    void shouldThrowExceptionForOverlappingSubtasksInSameEpic() {
        Epic epic = new Epic("Epic", "With overlaps");
        taskManager.addEpic(epic);

        Subtask sub1 = new Subtask(
                "Sub1",
                "Initial",
                0,
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(60),
                LocalDateTime.of(2023, 1, 1, 10, 0)
        );
        taskManager.addSubtask(sub1);

        Subtask sub2 = new Subtask(
                "Sub2",
                "Overlaps",
                0,
                Status.NEW,
                epic.getId(),
                Duration.ofMinutes(30),
                LocalDateTime.of(2023, 1, 1, 10, 30)
        );

        assertThrows(TimeConflictException.class, () -> taskManager.addSubtask(sub2));
    }

    // Проверка добавления задач с null startTime и duration
    @Test
    void shouldHandleNullStartTimeAndDuration() {
        Task task = new Task("Null Time", "No timing");
        taskManager.addTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loaded.getTaskById(task.getId());

        assertAll(
                () -> assertNotNull(loadedTask),
                () -> assertNull(loadedTask.getStartTime()),
                () -> assertNull(loadedTask.getDuration()),
                () -> assertEquals(task.getName(), loadedTask.getName())
        );
    }

    // Проверка эпика после удаления всех подзадач
    @Test
    void shouldResetEpicTimeAfterAllSubtasksRemoved() {
        Epic epic = new Epic("Epic", "Time test");
        taskManager.addEpic(epic);

        // Получаем ID, который реально присвоился после добавления
        int epicId = taskManager.getAllEpics().get(0).getId();

        Subtask subtask = new Subtask(
                "Sub1",
                "Initial",
                0,
                Status.NEW,
                epicId,
                Duration.ofMinutes(60),
                LocalDateTime.of(2023, 1, 1, 10, 0)
        );
        taskManager.addSubtask(subtask);

        assertNotNull(taskManager.getEpicById(epicId).getStartTime());

        taskManager.removeSubtaskById(subtask.getId());

        Epic updatedEpic = taskManager.getEpicById(epicId);

        assertAll(
                () -> assertEquals(Status.NEW, updatedEpic.getStatus()),
                () -> assertTrue(updatedEpic.getSubIds().isEmpty()),
                () -> assertNull(updatedEpic.getStartTime()),
                () -> assertNull(updatedEpic.getEndTime()),
                () -> assertNull(updatedEpic.getDuration())
        );
    }
}
