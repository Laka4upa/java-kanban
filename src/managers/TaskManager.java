package managers;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private int id;
    HashMap<Integer, Task> tasksHashMap;
    HashMap<Integer, Epic> epicsHashMap;
    HashMap<Integer, Subtask> subsHashMap;

    public TaskManager(){
        tasksHashMap = new HashMap<>();
        epicsHashMap = new HashMap<>();
        subsHashMap = new HashMap<>();
        this.id = 0;//инициализация счетчика
    }

    public int generateId() { // создание счетчика
        return ++id;
    }

    //a получение списка задач
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasksHashMap.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subsHashMap.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicsHashMap.values());
    }

    //b удаление всех задач
    public void removeAllTasks() {
        tasksHashMap.clear();
    }
    public void removeAllSubtasks() {
        subsHashMap.clear();
        for (Epic epic : epicsHashMap.values()) {
            epic.getSubIds().clear();
            updateEpic(epic);
        }
    }
    public void removeAllEpic() {
        epicsHashMap.clear();
        subsHashMap.clear();
    }

    //c получение по идентификатору
    public Task getTaskById(int id) {
        return tasksHashMap.get(id);
    }
    public Epic getEpicById(int id) {
        return epicsHashMap.get(id);
    }
    public Subtask getSubtaskById(int id) {
        return subsHashMap.get(id);
    }

    //d создание
    public void addTask(Task task) {
        task.setId(generateId());
        tasksHashMap.put(task.getId(),task);
    }

    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epicsHashMap.put(epic.getId(), epic);
    }
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subsHashMap.put(subtask.getId(), subtask);
        Epic epic = epicsHashMap.get(subtask.getEpicId());
        epic.getSubIds().add(subtask.getId());
        updateEpicStatus(epic);
    }

    //e обновление
    public void updateTask(Task task) {
        tasksHashMap.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        Epic oldEpic = epicsHashMap.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        epicsHashMap.put(oldEpic.getId(), oldEpic);
        updateEpicStatus(oldEpic);
    }

    public void updateSubtask(Subtask subtask) {
        subsHashMap.put(subtask.getId(), subtask);
        Epic epic = epicsHashMap.get(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    //f удаление по идентификатору
    public void removeTaskById(int id) {
        tasksHashMap.remove(id);
    }
    public void removeEpicById(int id) {
        Epic epic = getEpicById(id);
        for (int idSubtask: epic.getSubIds()) {
            subsHashMap.remove(idSubtask);
        }
        epicsHashMap.remove(id);
    }
    public void removeSubtaskById(int id) {
        Subtask subtask = subsHashMap.remove(id);
        Epic epic = getEpicById(subtask.getEpicId());
        epic.getSubIds().remove(Integer.valueOf(id));
        updateEpicStatus(epic);
    }

    //получение списка всех подзадач определенного эпика
    public ArrayList<Subtask> getAllSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer idSubtask : epic.getSubIds()) {
            subtasksList.add(getSubtaskById(idSubtask));
        }
        return subtasksList;
    }

    //обновление статуса у эпиков
    private void updateEpicStatus(Epic epic) {
        int newCount = 0;
        int doneCount = 0;
        if (epic.getSubIds().isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }
        for (Integer idSub : epic.getSubIds()) {
            if (getSubtaskById(idSub).getStatus() == TaskStatus.NEW) {
                newCount++;
            }
            if (getSubtaskById(idSub).getStatus() == TaskStatus.DONE) {
                doneCount++;
            }
        }
        int countOfSubtasks = epic.getSubIds().size();
        if (newCount == countOfSubtasks) {
            epic.setStatus(TaskStatus.NEW);
        } else if (doneCount == countOfSubtasks) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}


