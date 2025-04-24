package managers;

import tasks.*;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InMemoryTaskManagerTest {

    @Test
    void shouldAddAllTasksTypesAndGetById() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task task = new Task("Task1", "Description1");
        taskManager.addTask(task);
        Epic epic = new Epic("Epic1", "Description2");
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Description3", epic.getId());
        taskManager.addSubtask(subtask);
        //Проверяем, что ID были сгенерированы правильно
        assertTrue(task.getId() >= 0, "Task должен получить ID");
        assertTrue(epic.getId() >= 0, "Epic должен получить ID");
        assertTrue(subtask.getId() >= 0, "Subtask должен получить ID");
        //Проверяем получение задач по ID
        assertEquals(task, taskManager.getTaskById(task.getId())); // По id должна вернуться та же задача
        assertEquals(epic, taskManager.getEpicById(epic.getId()));
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void shouldNotConflictSetIdAndGeneratedId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task manualTask = new Task("Manual", "Description");
        manualTask.setId(100);  // Явно задаем большой ID
        taskManager.addTask(manualTask);
        //Добавляем несколько задач с авто-генерацией
        Task autoTask1 = new Task("Auto1", "Description1");
        Task autoTask2 = new Task("Auto2", "Description2");
        taskManager.addTask(autoTask1);
        taskManager.addTask(autoTask2);
        //Проверяем что все задачи сохранились
        assertEquals(3, taskManager.getAllTasks().size(), "Должны быть все 3 задачи");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task originalTask = new Task("Original Name", "Original Description", 1, Status.NEW);
        taskManager.addTask(originalTask);
        //Меняем оригинальную задачу
        originalTask.setName("New Name");
        originalTask.setDescription("New Description");
        originalTask.setStatus(Status.DONE);
        //Проверяем, что задача в менеджере не изменилась
        Task storedTask = taskManager.getTaskById(1);

        assertEquals("Original Name", storedTask.getName());
        assertEquals("Original Description", storedTask.getDescription());
        assertEquals(Status.NEW, storedTask.getStatus());
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        tm.addTask(task1); // id = 1, счетчик начинается с 1
        tm.addTask(task2); // id = 2
        assertEquals(task1.getId() + 1, task2.getId()); // id второй задачи должен быть на 1 больше первого
    }
}