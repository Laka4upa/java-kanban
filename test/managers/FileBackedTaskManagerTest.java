package managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tasks.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManagerTest {

    // Сохранение и загрузка пустого файла
    @Test
    public void testEmptyFileSaveAndLoad() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        // Сохраняем пустой менеджер
        manager.save();
        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        // Проверяем, что задачи не загрузились
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    // Сохранение нескольких задач
    @Test
    public void testSaveMultipleTasks() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        // Добавляем задачи разных типов
        Task task = new Task("Task 1", "Description");
        manager.addTask(task);

        Epic epic = new Epic("Epic 1", "Epic Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", epic.getId());
        manager.addSubtask(subtask);
        // Проверяем, что файл не пустой
        assertTrue(tempFile.length() > 0);
        // Проверяем, что файл содержит все задачи
        String content = Files.readString(tempFile.toPath());
        assertTrue(content.contains("Task 1"));
        assertTrue(content.contains("Epic 1"));
        assertTrue(content.contains("Subtask 1"));
    }

    // Загрузка нескольких задач
    @Test
    public void shouldSaveAndLoadTasks() throws IOException {
        File tempFile = File.createTempFile("tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        // Добавляем задачи разных типов
        Task task = new Task("Task 1", "Description 1");
        manager.addTask(task);

        Epic epic = new Epic("Epic 1", "Epic description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Sub description", epic.getId());
        manager.addSubtask(subtask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем задачи
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        // Проверяем содержимое задач
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());

        // Проверяем связь подзадачи с эпиком
        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());
        assertTrue(loadedManager.getEpicById(epic.getId()).getSubIds().contains(subtask.getId()));
    }
}