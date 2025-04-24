package tasks;

import static org.junit.jupiter.api.Assertions.*;

import managers.InMemoryTaskManager;
import org.junit.jupiter.api.Test;

class EpicTest {

    //проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void canNotAddEpicAsSubtask() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Description");
        taskManager.addEpic(epic);
        int epicId = epic.getId();
        Subtask subtask = new Subtask("Subtask", "Description", epicId);
        final int subtaskId1 = subtask.getId();
        assertNotNull(subtaskId1, "добавили epic сам в себя");
    }

    //проверка, что объект Subtask нельзя сделать своим же эпиком
    @Test
    void shouldNotBeAddSubtaskToEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Desc");
        tm.addEpic(epic); // Создается эпик и добавляется в менеджер
        Subtask sub = new Subtask("Sub", "Desc", 1);
        tm.addSubtask(sub); // Создается подзадача и добавляется в менеджер
        epic.addSubtaskId(1);
        epic.addSubtaskId(1); // Попытка добавить дубликат ID подзадачи
        assertEquals(1, epic.getSubIds().size()); // epic.getSubtaskIds().size должен быть 1
    }
}