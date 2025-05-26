package managers;

import exceptions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tasks.*;
import util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final String CSV_HEADER = "id,type,name,status,description,epic,duration,startTime,endTime\n";

    public FileBackedTaskManager(File file) {
        Objects.requireNonNull(file, "Файл не может быть null");
        this.file = file;
    }

    // Метод для сохранения текущего состояния в файл
    protected void save() {
        try {
            // Создаем директории только если путь содержит поддиректории
            Path path = file.toPath();
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                // Заголовок CSV
                writer.write(CSV_HEADER);
                // Сохраняем задачи
                for (Task task : getAllTasks()) {
                    writer.write(toString(task) + "\n");
                }
                // Сохраняем эпики
                for (Epic epic : getAllEpics()) {
                    writer.write(toString(epic) + "\n");
                }
                // Сохраняем подзадачи
                for (Subtask subtask : getAllSubtasks()) {
                    writer.write(toString(subtask) + "\n");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    // Метод загрузки данных из файла в память
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        int maxId = 0;

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            // Пропускаем заголовок
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1); // -1 сохраняет пустые значения
                if (parts.length < 6) {
                    throw new ManagerSaveException("Некорректная строка: " + line);
                }
                Task task = fromString(line);
                if (task == null) continue;
                // Обновляем максимальный ID
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
                // Распределяем задачи по соответствующим мапам
                switch (task.getType()) {
                    case "TASK":
                        manager.tasks.put(task.getId(), task);
                        break;
                    case "EPIC":
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case "SUBTASK":
                        Subtask subtask = (Subtask) task;
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subtask.getId());
                        }
                        break;
                    default:
                        throw new ManagerSaveException("Неизвестный тип задачи: " + task.getType());
                }
            }
            // Обновляем статусы и временные параметры эпиков
            manager.epics.values().forEach(epic -> {
                manager.updateEpicStatus(epic);
                epic.updateTimeParameters(manager.getAllSubtasksOfEpic(epic));
            });
            manager.id = maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
        return manager;
    }

    // Преобразование задачи в строку CSV
    private String toString(Task task) {
        return task.toCsv();
    }

    // Преобразование строки CSV в объект задачи
    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ManagerSaveException("Строка не может быть пустой");
        }

        String[] values = parseCsvLine(value);
        if (values.length < 6) {
            throw new ManagerSaveException("Недостаточно данных в строке: " + value);
        }

        try {
            int id = Integer.parseInt(values[0].trim());
            String type = values[1].trim();
            String name = values[2].trim();
            Status status = Status.valueOf(values[3].trim().toUpperCase());
            String description = values.length > 4 ? values[4].trim() : "";
            Duration duration = null;
            LocalDateTime startTime = null;
            if (values.length > 6 && !values[6].isEmpty()) {
                duration = Duration.ofMinutes(Long.parseLong(values[6].trim()));
            }
            if (values.length > 7 && !values[7].isEmpty()) {
                startTime = LocalDateTime.parse(values[7].trim());
            }

            switch (type) {
                case "TASK":
                    Task task = new Task(name, description);
                    task.setId(id);
                    task.setStatus(status);
                    task.setDuration(duration);
                    task.setStartTime(startTime);
                    return task;
                case "EPIC":
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    epic.setDuration(duration);
                    epic.setStartTime(startTime);
                    return epic;
                case "SUBTASK":
                    if (values.length < 6 || values[5].trim().isEmpty()) {
                        throw new ManagerSaveException("Для подзадачи не указан ID эпика");
                    }
                    int epicId = Integer.parseInt(values[5].trim());
                    Subtask subtask = new Subtask(name, description, epicId);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    subtask.setDuration(duration);
                    subtask.setStartTime(startTime);
                    return subtask;
                default:
                    throw new ManagerSaveException("Неизвестный тип задачи: " + type);
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Ошибка формата числа в строке: " + value, e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Некорректное значение статуса в строке: " + value, e);
        }
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }

    @Override
    public void updateEpicTimeParameters(Epic epic) {
        List<Subtask> subtasks = getAllSubtasksOfEpic(epic);
        epic.updateTimeParameters(subtasks);
    }

    // Переопределяем методы, изменяющие состояние, и добавляем сохранение
    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        Epic epic = getEpicById(subtask.getEpicId());
        if (epic != null) {
            epic.updateTimeParameters(getAllSubtasksOfEpic(epic));
        }
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        Epic epic = getEpicById(subtask.getEpicId());
        if (epic != null) {
            epic.updateTimeParameters(getAllSubtasksOfEpic(epic));
        }
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(int id) {
        Subtask subtask = getSubtaskById(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            super.removeSubtaskById(id);
            Epic epic = getEpicById(epicId);
            if (epic != null) {
                epic.updateTimeParameters(getAllSubtasksOfEpic(epic));
            }
            save();
        }
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        for (Epic epic : getAllEpics()) {
            epic.clearSubtaskIds();
            epic.updateTimeParameters(List.of());
        }
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }
}
