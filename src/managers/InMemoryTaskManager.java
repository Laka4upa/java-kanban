package managers;

import java.util.*;
import java.util.stream.Collectors;

import tasks.*;
import util.Managers;
import util.Status;
import exceptions.TimeConflictException;

public class InMemoryTaskManager implements TaskManager {
    static int id;
    final HashMap<Integer, Task> tasks;
    final HashMap<Integer, Epic> epics;
    final HashMap<Integer, Subtask> subtasks;
    final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            // задачи со startTime == null идут в конец
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

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
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.clearSubtaskIds();
            updateEpicStatus(epic);
        });
    }

    @Override
    public void removeAllEpics() {
        subtasks.keySet().forEach(historyManager::remove);
        epics.keySet().forEach(historyManager::remove);
        subtasks.clear();
        epics.clear();
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
        if (hasTimeOverlap(task)) { // Добавление проверки по пересечению задач
            throw new TimeConflictException("Задача пересекается по времени с существующей.");
        }
        if (task.getId() <= 0 || tasks.containsKey(task.getId())) {
            task.setId(generateId());
        }
        Task taskCopy = task.copy();
        tasks.put(taskCopy.getId(), taskCopy);
        if (taskCopy.getStartTime() != null) {
            prioritizedTasks.add(taskCopy);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        if (epic.getId() <= 0 || epics.containsKey(epic.getId())) {
            epic.setId(generateId());
        }
        Epic epicCopy = new Epic(epic.getName(), epic.getDescription());
        epicCopy.setId(epic.getId());
        epicCopy.setStatus(Status.NEW);
        epicCopy.getSubIds().addAll(epic.getSubIds());
        epics.put(epicCopy.getId(), epicCopy);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask == null) return;
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени");
        }
        if (subtask.getId() <= 0 || subtasks.containsKey(subtask.getId())) {
            subtask.setId(generateId());
        }
        if (subtasks.containsKey(subtask.getId())) return;

        Subtask subtaskCopy = subtask.copy();

        if (!epics.containsKey(subtask.getEpicId())) return;
        subtasks.put(subtaskCopy.getId(), subtaskCopy);
        if (subtaskCopy.getStartTime() != null) {
            prioritizedTasks.add(subtaskCopy);
        }
        Epic epic = epics.get(subtaskCopy.getEpicId());
        if (epic.addSubtaskId(subtaskCopy.getId())) {
            updateEpicStatus(epic);
        }
        updateEpicTimeParameters(epic);
    }

    @Override
    public void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            if (hasTimeOverlap(task)) {
                throw new TimeConflictException("Задача пересекается по времени с существующей.");
            }
            Task oldTask = tasks.get(task.getId());
            prioritizedTasks.remove(oldTask); // Удаляем старую версию

            Task taskCopy = task.copy();
            tasks.put(taskCopy.getId(), taskCopy);

            if (taskCopy.getStartTime() != null) {
                prioritizedTasks.add(taskCopy); // Добавляем обновленную
            }
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
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Обновленная подзадача пересекается по времени с существующими");
        }
        Subtask oldSubtask = subtasks.get(subtask.getId());
        prioritizedTasks.remove(oldSubtask);

        Subtask subtaskCopy = subtask.copy();
        subtasks.put(subtaskCopy.getId(), subtaskCopy);

        if (subtaskCopy.getStartTime() != null) {
            prioritizedTasks.add(subtaskCopy);
        }
        updateEpicStatus(epics.get(subtaskCopy.getEpicId()));
    }

    @Override
    public void removeTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }


    @Override
    public void removeEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            epic.getSubIds().stream()
                    .forEach(subtaskId -> {
                        subtasks.remove(subtaskId);
                        historyManager.remove(subtaskId);
                    });
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.removeSubtaskById(id);
                updateEpicStatus(epic);
                updateEpicTimeParameters(epic);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(Epic epic) {
        return epic.getSubIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic.getSubIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        Map<Status, Long> statusCount = epic.getSubIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Subtask::getStatus,
                        Collectors.counting()
                ));
        long newCount = statusCount.getOrDefault(Status.NEW, 0L);
        long doneCount = statusCount.getOrDefault(Status.DONE, 0L);
        int totalSubtasks = epic.getSubIds().size();

        if (newCount == totalSubtasks) {
            epic.setStatus(Status.NEW);
        } else if (doneCount == totalSubtasks) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null) // Исключаем задачи без времени
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .anyMatch(existingTask -> isTimeOverlap(existingTask, newTask));
    }

    private boolean isTimeOverlap(Task a, Task b) {
        return a.getStartTime().isBefore(b.getEndTime())
                && a.getEndTime().isAfter(b.getStartTime());
    }

    @Override
    public void updateEpicTimeParameters(Epic epic) {
        List<Subtask> subtasks = getAllSubtasksOfEpic(epic);
        epic.updateTimeParameters(subtasks);
    }
}