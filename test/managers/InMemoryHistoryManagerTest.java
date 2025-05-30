package managers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.*;
import tasks.*;
import util.Managers;


class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddToHistory() {
        Task task = new Task("Test", "Description");
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task, historyManager.getHistory().get(0));
    }

    @Test
    void shouldNotDuplicateInHistory() {
        Task task = new Task("Test", "Description");
        historyManager.add(task);
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void shouldRemoveFromHistory() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        System.out.println("Исходная история: " + historyManager.getHistory());

        // Удаление из начала
        historyManager.remove(task1.getId());
        System.out.println("После удаления task1: " + historyManager.getHistory());
        assertEquals(List.of(task2, task3), historyManager.getHistory());

        // Удаление из середины
        historyManager.add(task1);
        System.out.println("После добавления task1: " + historyManager.getHistory());
        historyManager.remove(task3.getId());
        System.out.println("После удаления task3: " + historyManager.getHistory());
        assertEquals(List.of(task2, task1), historyManager.getHistory());

        // Удаление из конца
        historyManager.remove(task1.getId());
        System.out.println("После удаления task1: " + historyManager.getHistory());
        assertEquals(List.of(task2), historyManager.getHistory());
    }

    @Test
    void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }
}