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

    public InMemoryTaskManager() {
        this.tasksHashMap = new HashMap<>();
        this.epicsHashMap = new HashMap<>();
        this.subsHashMap = new HashMap<>();
        this.id = 0; //инициализация счетчика
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
            epic.clearSubtaskIds();
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
        if (task == null) {
            return null;
        }
        historyManager.add(task.copy()); // Добавляем копию в историю
        return task.copy(); // Возвращаем копию
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epicsHashMap.get(id);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic.copy()); // Добавляем копию в историю
        return epic.copy(); // Возвращаем копию
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subsHashMap.get(id);
        if (subtask == null) {
            return null;
        }
        historyManager.add(subtask.copy()); // Добавляем копию в историю
        return subtask.copy(); // Возвращаем копию
    }

    @Override
    public void addTask(Task task) {
        if (task == null) return;
        task.setId(generateId());
        Task taskCopy = task.copy();
        tasksHashMap.put(taskCopy.getId(), taskCopy);
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

        Subtask subtaskCopy = subtask.copy();

        if (!epicsHashMap.containsKey(subtask.getEpicId())) return;
        subsHashMap.put(subtaskCopy.getId(), subtaskCopy);
        Epic epic = epicsHashMap.get(subtaskCopy.getEpicId());
        if (epic.addSubtaskId(subtaskCopy.getId())) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasksHashMap.containsKey(task.getId())) {
            Task taskCopy = task.copy();
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
        Subtask subtaskCopy = subtask.copy();
        subsHashMap.put(subtaskCopy.getId(), subtaskCopy);
        updateEpicStatus(epicsHashMap.get(subtaskCopy.getEpicId()));
    }

    @Override
    public void removeTaskById(int id) {
        tasksHashMap.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epicsHashMap.get(id);
        if (epic.getSubIds() != null) {
            for (int idSubtask : epic.getSubIds()) {
                subsHashMap.remove(idSubtask);
                historyManager.remove(idSubtask);
            }
        }
        historyManager.remove(id);
        epicsHashMap.remove(id);
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subsHashMap.remove(id);
        Epic epic = getEpicById(subtask.getEpicId());
        epic.removeSubtaskId(id);
        updateEpicStatus(epic);
        historyManager.remove(id);
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer idSubtask : epic.getSubIds()) {
            subtasksList.add(subsHashMap.get(idSubtask));
        }
        return subtasksList;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        int newCount = 0;
        int doneCount = 0;
        if (epic.getSubIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        for (Integer idSub : epic.getSubIds()) {
            if (subsHashMap.get(idSub).getStatus() == Status.NEW) {
                newCount++;
            }
            if (subsHashMap.get(idSub).getStatus() == Status.DONE) {
                doneCount++;
            }
        }
        int countOfSubtasks = epic.getSubIds().size();
        if (newCount == countOfSubtasks) {
            epic.setStatus(Status.NEW);
        } else if (doneCount == countOfSubtasks) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}