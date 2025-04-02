package managers;

import org.junit.jupiter.api.Test;
import tasks.Task;
import tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyKeepsPreviousTaskVersions() {
        TaskManager taskManager = Managers.getDefault();
        Task oldTask = new Task("tit1", "dis1", TaskStatus.NEW);
        taskManager.addTask(oldTask);
        taskManager.getTaskById(1);
        Task updTask = new Task("tit11", "dis11", TaskStatus.IN_PROGRESS);
        updTask.setId(1);
        taskManager.updateTask(updTask);
        Task fromHistoryTask = taskManager.getHistory().getFirst();
        assertEquals(fromHistoryTask, oldTask, "В историю сохранена другая версия");
        assertEquals(taskManager.getTaskById(1), updTask, "В массиве лежит не новая задача ");
    }

    //проверка, что в историю записывается только 10 задач
    @Test
    public void shouldBe10TasksInHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Task task = new Task("Name1","Description1");
        int expectedTasks = 10;
        for (int i = 0; i <= 13; i++) {
            historyManager.add(task);
        }
        assertEquals(expectedTasks, historyManager.getHistory().size(), "Задач больше 10");
    }
}