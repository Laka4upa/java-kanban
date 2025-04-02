package managers;

import tasks.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int id;
    private final HashMap<Integer, Task> tasksHashMap;
    private final HashMap<Integer, Epic> epicsHashMap;
    private final HashMap<Integer, Subtask> subsHashMap;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(){
        this.tasksHashMap = new HashMap<>();
        this.epicsHashMap = new HashMap<>();
        this.subsHashMap = new HashMap<>();
        this.id = 0;//инициализация счетчика
        this.historyManager = Managers.getDefaultHistory();
    }

    public int generateId() { // создание счетчика
        return ++id;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasksHashMap.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subsHashMap.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epicsHashMap.values());
    }

    @Override
    public void removeAllTasks() {
        tasksHashMap.clear();
    }
    @Override
    public void removeAllSubtasks() {
        subsHashMap.clear();
        for (Epic epic : epicsHashMap.values()) {
            epic.getSubIds().clear();
            updateEpic(epic);
        }
    }
    @Override
    public void removeAllEpic() {
        epicsHashMap.clear();
        subsHashMap.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasksHashMap.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicsHashMap.get(id);
        if (epic != null) {
            historyManager.add(epic);
            // Возвращаем копию
            Epic copy = new Epic(epic.getName(), epic.getDescription());
            copy.setId(epic.getId());
            copy.setStatus(epic.getStatus());
            copy.getSubIds().addAll(epic.getSubIds());
            return copy;
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask original = subsHashMap.get(id);
        if (original == null) return null;
        historyManager.add(original);
        // Возвращаем копию
        return new Subtask(
                original.getName(),
                original.getDescription(),
                original.getId(),
                original.getStatus(),
                original.getEpicId()
        );
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        task.setId(generateId());
        Task taskCopy = new Task(
                task.getName(),
                task.getDescription(),
                task.getId(),
                task.getStatus()
        );
        tasksHashMap.put(taskCopy.getId(),taskCopy);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(generateId());
        Epic epicCopy = new Epic(epic.getName(), epic.getDescription());
        epicCopy.setId(epic.getId());
        epicCopy.setStatus(epic.getStatus());
        epicCopy.getSubIds().addAll(epic.getSubIds());
        epicsHashMap.put(epicCopy.getId(), epicCopy);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        subtask.setId(generateId());
        if (subsHashMap.containsKey(subtask.getId())) return;
        Subtask subtaskCopy = new Subtask(
                subtask.getName(),
                subtask.getDescription(),
                subtask.getId(),
                subtask.getStatus(),
                subtask.getEpicId()
        );
        if (!epicsHashMap.containsKey(subtask.getEpicId())) return;

        subsHashMap.put(subtaskCopy.getId(), subtaskCopy);
        Epic epic = epicsHashMap.get(subtaskCopy.getEpicId());
        if (!epic.getSubIds().contains(subtaskCopy.getId())) {
            epic.getSubIds().add(subtaskCopy.getId());
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasksHashMap.containsKey(task.getId())) {
            Task taskCopy = new Task(
                    task.getName(),
                    task.getDescription(),
                    task.getId(),
                    task.getStatus()
            );
            tasksHashMap.put(taskCopy.getId(), taskCopy);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epicsHashMap.containsKey(epic.getId())) {
            return;
        }
        Epic oldEpic = epicsHashMap.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        epicsHashMap.put(oldEpic.getId(), oldEpic);
        updateEpicStatus(oldEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subsHashMap.containsKey(subtask.getId())) {
            return;
        }
        // Создаем копию для обновления
        Subtask subtaskCopy = new Subtask(
                subtask.getName(),
                subtask.getDescription(),
                subtask.getId(),
                subtask.getStatus(),
                subtask.getEpicId()
        );
        subsHashMap.put(subtaskCopy.getId(), subtaskCopy);
        updateEpicStatus(epicsHashMap.get(subtaskCopy.getEpicId()));
    }

    @Override
    public void removeTaskById(int id) {
        tasksHashMap.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = getEpicById(id);
        for (int idSubtask: epic.getSubIds()) {
            subsHashMap.remove(idSubtask);
        }
        epicsHashMap.remove(id);
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subsHashMap.remove(id);
        Epic epic = getEpicById(subtask.getEpicId());
        epic.getSubIds().remove(Integer.valueOf(id));
        updateEpicStatus(epic);
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer idSubtask : epic.getSubIds()) {
            subtasksList.add(getSubtaskById(idSubtask));
        }
        return subtasksList;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
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

    @Override
    public List<Task> getHistory(){
        return historyManager.getHistory();
    }
}