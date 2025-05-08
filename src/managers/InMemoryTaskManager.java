package managers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import tasks.*;
import util.Managers;
import util.Status;

public class InMemoryTaskManager implements TaskManager {
    int id;
    final HashMap<Integer, Task> tasks;
    final HashMap<Integer, Epic> epics;
    final HashMap<Integer, Subtask> subtasks;
    final HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.id = 0; //инициализация счетчика
        this.historyManager = Managers.getDefaultHistory();
    }

    public int generateId() { // создание счетчика
        return ++id;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void removeAllTasks() {
        for (Integer taskId : tasks.keySet()) {
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpic(epic);
        }
    }

    @Override
    public void removeAllEpics() {
        for (Integer subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }
        for (Integer epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            return null;
        }
        historyManager.add(task.copy()); // Добавляем копию в историю
        return task.copy(); // Возвращаем копию
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        historyManager.add(epic.copy()); // Добавляем копию в историю
        return epic.copy(); // Возвращаем копию
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
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
        tasks.put(taskCopy.getId(), taskCopy);
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(generateId());
        Epic epicCopy = new Epic(epic.getName(), epic.getDescription());
        epicCopy.setId(epic.getId());
        epicCopy.setStatus(Status.NEW);
        epicCopy.getSubIds().addAll(epic.getSubIds());
        epics.put(epicCopy.getId(), epicCopy);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        subtask.setId(generateId());
        if (subtasks.containsKey(subtask.getId())) return;

        Subtask subtaskCopy = subtask.copy();

        if (!epics.containsKey(subtask.getEpicId())) return;
        subtasks.put(subtaskCopy.getId(), subtaskCopy);
        Epic epic = epics.get(subtaskCopy.getEpicId());
        if (epic.addSubtaskId(subtaskCopy.getId())) {
            updateEpicStatus(epic);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            Task taskCopy = task.copy();
            tasks.put(taskCopy.getId(), taskCopy);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getId())) {
            return;
        }
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        epics.put(oldEpic.getId(), oldEpic);
        updateEpicStatus(oldEpic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getId())) {
            return;
        }
        Subtask subtaskCopy = subtask.copy(); // Создаем копию для обновления
        subtasks.put(subtaskCopy.getId(), subtaskCopy);
        updateEpicStatus(epics.get(subtaskCopy.getEpicId()));
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic.getSubIds() != null) {
            for (int idSubtask : epic.getSubIds()) {
                subtasks.remove(idSubtask);
                historyManager.remove(idSubtask);
            }
        }
        historyManager.remove(id);
        epics.remove(id);
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskById(id);
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksOfEpic(Epic epic) {
        ArrayList<Subtask> subtasksList = new ArrayList<>();
        for (Integer idSubtask : epic.getSubIds()) {
            subtasksList.add(subtasks.get(idSubtask));
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
            if (subtasks.get(idSub).getStatus() == Status.NEW) {
                newCount++;
            }
            if (subtasks.get(idSub).getStatus() == Status.DONE) {
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