package tasks;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TaskTest {
    //проверка, что экземпляры класса Task равны если равны их ID
    @Test
    void shouldBePositiveIfTaskIdAreEqual() {
        Task task1 = new Task("Name1", "Description1");
        Task task2 = new Task("Name2", "Description2");
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2, "задачи не совпадают");
    }

    @Test
    void shouldBeNegativeIfTaskIdAreDifferent() {
        Task task1 = new Task("Name1", "Description1");
        Task task2 = new Task("Name2", "Description2");
        task1.setId(1);
        task2.setId(2);
        assertNotEquals(task1, task2, "задачи совпадают");
    }

    //проверка, что наследники класса Task равны друг другу, если равен их id;
    @Test
    void tasksInheritorsAreEqualIfHasSameIds() {
        Subtask task1 = new Subtask("Task1", "Desc1", 1);
        Epic task2 = new Epic("Task2", "Desc2");
        task1.setId(1);
        task2.setId(1); // id = 1
        assertEquals(task1, task2); // Должны быть равны
    }
}