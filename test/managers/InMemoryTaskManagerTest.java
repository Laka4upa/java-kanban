package managers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tasks.*;
import util.Status;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    // Специфичные тесты для InMemoryTaskManager
    @Test
    void shouldAddAllTasksTypesAndGetById() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("Task1", "Description1");
        taskManager.addTask(task);
        int taskId = task.getId();

        Epic epic = new Epic("Epic1", "Description2");
        taskManager.addEpic(epic);
        int epicId = epic.getId();  // Получаем ID созданного эпика

        Subtask subtask = new Subtask("Subtask1", "Description3", epicId);
        taskManager.addSubtask(subtask);
        int subtaskId = subtask.getId();

        assertTrue(taskId >= 0, "Task должен получить ID");
        assertTrue(epicId >= 0, "Epic должен получить ID");
        assertTrue(subtaskId >= 0, "Subtask должен получить ID");

        Task retrievedTask = taskManager.getTaskById(taskId);
        assertEquals(task, retrievedTask);
        assertNotSame(task, retrievedTask, "Должна возвращаться копия задачи");

        Epic retrievedEpic = taskManager.getEpicById(epicId);
        // Используем retrievedEpic для актуального состояния
        assertEquals("Epic1", retrievedEpic.getName());
        assertEquals("Description2", retrievedEpic.getDescription());
        assertEquals(Status.NEW, retrievedEpic.getStatus());
        assertTrue(retrievedEpic.getSubIds().contains(subtaskId), "Epic должен содержать ID подзадачи");
        assertNotSame(epic, retrievedEpic, "Должна возвращаться копия эпика");

        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtask, retrievedSubtask);
        assertNotSame(subtask, retrievedSubtask, "Должна возвращаться копия подзадачи");
    }

    @Test
    void shouldNotConflictSetIdAndGeneratedId() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task manualTask = new Task("Manual", "Description");
        manualTask.setId(100);
        taskManager.addTask(manualTask);

        Task autoTask1 = new Task("Auto1", "Description1");
        Task autoTask2 = new Task("Auto2", "Description2");
        taskManager.addTask(autoTask1);
        taskManager.addTask(autoTask2);

        assertEquals(3, taskManager.getAllTasks().size(), "Должны быть все 3 задачи");
        assertEquals(100, manualTask.getId(), "ID ручной задачи не должен измениться");
        assertNotEquals(100, autoTask1.getId(), "Автосгенерированные ID должны отличаться");
        assertNotEquals(100, autoTask2.getId(), "Автосгенерированные ID должны отличаться");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddingToManager() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Task originalTask = new Task("Original Name", "Original Description", 1, Status.NEW);
        taskManager.addTask(originalTask);

        originalTask.setName("New Name");
        originalTask.setDescription("New Description");
        originalTask.setStatus(Status.DONE);

        Task storedTask = taskManager.getTaskById(1);
        assertEquals("Original Name", storedTask.getName());
        assertEquals("Original Description", storedTask.getDescription());
        assertEquals(Status.NEW, storedTask.getStatus());

        // Проверяем, что изменения оригинальной задачи не повлияли на копию
        assertNotEquals(originalTask.getName(), storedTask.getName());
        assertNotEquals(originalTask.getDescription(), storedTask.getDescription());
        assertNotEquals(originalTask.getStatus(), storedTask.getStatus());
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        tm.addTask(task1);
        tm.addTask(task2);

        assertEquals(task1.getId() + 1, task2.getId());
        assertNotEquals(task1.getId(), task2.getId(), "ID задач должны быть уникальными");
    }

    @Test
    void testTaskCopyEquality() {
        Task original = new Task("Test", "Desc", 1, Status.IN_PROGRESS);
        Task copy = original.copy();

        assertEquals(original, copy, "Копия должна быть равна оригиналу");
        assertNotSame(original, copy, "Копия должна быть отдельным объектом");
    }

    @Test
    void testSubtaskCopyWithEpicId() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(10);
        Subtask original = new Subtask("Sub", "Desc", 1, Status.NEW, epic.getId());
        Subtask copy = original.copy();

        assertEquals(original.getEpicId(), copy.getEpicId(), "EpicID должен сохраняться при копировании");
        assertEquals(original, copy);
        assertNotSame(original, copy);
    }

    @Test
    void testEpicCopyWithSubtasks() {
        Epic original = new Epic("Epic", "Desc");
        original.addSubtaskId(1);
        original.addSubtaskId(2);
        Epic copy = original.copy();

        assertEquals(original.getSubIds(), copy.getSubIds(), "Список подзадач должен копироваться");
        assertNotSame(original.getSubIds(), copy.getSubIds(), "Список подзадач должен быть новым объектом");
    }
}