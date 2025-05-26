package managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import tasks.*;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import exceptions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        assertTrue(tempFile.exists());
        assertTrue(tempFile.canWrite());
        super.setUp();  // Теперь tempFile уже создан
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
        // Добавляем тестовые данные
        Epic epic = new Epic("Test Epic", "Description");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Desc", epic.getId());
        taskManager.addSubtask(subtask);

        Task task = new Task("Test Task", "Description");
        taskManager.addTask(task);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertAll(
                () -> assertEquals(1, loadedManager.getAllTasks().size()),
                () -> assertEquals(1, loadedManager.getAllEpics().size()),
                () -> assertEquals(1, loadedManager.getAllSubtasks().size())
        );
    }
}
