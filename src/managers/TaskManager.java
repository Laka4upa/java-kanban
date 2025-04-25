package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    //a получение списка задач
    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    //b удаление всех задач
    void removeAllTasks();

    void removeAllSubtasks();

    void removeAllEpics();

    //c получение по идентификатору
    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    //d создание
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    //e обновление
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    //f удаление по идентификатору
    void removeTaskById(int id);

    void removeEpicById(int id);

    void removeSubtaskById(int id);

    //получение списка всех подзадач определенного эпика
    ArrayList<Subtask> getAllSubtasksOfEpic(Epic epic);

    //обновление статуса у эпиков
    void updateEpicStatus(Epic epic);

    //получение истории
    List<Task> getHistory();
}
