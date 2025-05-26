package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    void removeAllTasks();

    void removeAllSubtasks();

    void removeAllEpics();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void removeTaskById(int id);

    void removeEpicById(int id);

    void removeSubtaskById(int id);

    List<Subtask> getAllSubtasksOfEpic(Epic epic);

    void updateEpicStatus(Epic epic);

    List<Task> getHistory();

    //Получить список задач в порядке приоритета (по startTime)
    List<Task> getPrioritizedTasks();

    /**
     * Проверить пересечение по времени с существующими задачами
     * @param task задача для проверки
     * @return true если есть пересечение по времени
     */
    boolean hasTimeOverlap(Task task);

    /**
     * Обновить временные параметры эпика
     * @param epic эпик для обновления
     */
    void updateEpicTimeParameters(Epic epic);
}
