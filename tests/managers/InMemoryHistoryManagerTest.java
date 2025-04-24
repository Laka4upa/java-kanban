package managers;

import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyKeepsPreviousTaskVersions() {
        TaskManager taskManager = Managers.getDefault();
        Task oldTask = new Task("tit1", "dis1", Status.NEW);
        taskManager.addTask(oldTask);
        taskManager.getTaskById(1);
        Task updTask = new Task("tit11", "dis11", Status.IN_PROGRESS);
        updTask.setId(1);
        taskManager.updateTask(updTask);
        Task fromHistoryTask = taskManager.getHistory().getFirst();
        assertEquals(fromHistoryTask, oldTask, "В историю сохранена другая версия");
        assertEquals(taskManager.getTaskById(1), updTask, "В массиве лежит не новая задача ");
    }

    @Test
    void testOrderOfTasks() { // Тест на порядок в истории
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.IN_PROGRESS);
        task2.setId(2);

        hm.add(task1);
        hm.add(task2);

        List<Task> history = hm.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveTask() { // Тест на удаление
        HistoryManager hm = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc", Status.NEW);
        hm.add(task);
        hm.remove(task.getId());
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testEmptyHistory() { // Тест на пустую историю
        HistoryManager hm = new InMemoryHistoryManager();
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testNoDuplicatesSameId() { // Тест на отсутствие дубликатов
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.IN_PROGRESS);
        task2.setId(1); // Тот же id

        hm.add(task1);
        hm.add(task2); // Удаляет task1 и добавляет task2

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size()); // Только task2
        assertEquals(task2, history.get(0));
    }

    @Test
    void lastViewIsAddedToTheEnd() { // Тест на добавление в конец истории
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);

        task1.setId(1);
        task2.setId(2);

        hm.add(task1); // Добавляем первый просмотр
        hm.add(task2); // Добавляем второй просмотр

        List<Task> history = hm.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(1)); // Последний должен быть в конце
    }
}