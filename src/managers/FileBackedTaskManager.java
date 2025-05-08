package managers;

import exceptions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import tasks.*;
import util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Метод для сохранения текущего состояния в файл
    protected void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            // Заголовок CSV
            writer.write("id,type,name,status,description,epic\n");
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
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        } catch (Exception e) {
            throw new ManagerSaveException("Неожиданная ошибка при сохранении", e);
        }
    }

    // Метод загрузки данных из файла в память
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            // Пропускаем заголовок
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue; // Пропускаем пустые строки
                }
                // Восстанавливаем задачи
                Task task = fromString(line);
                if (task instanceof Epic) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else if (task instanceof Subtask) {
                    manager.subtasks.put(task.getId(), (Subtask) task);
                    // Добавляем подзадачу в эпик
                    Epic epic = manager.epics.get(((Subtask) task).getEpicId());
                    if (epic != null) {
                        epic.addSubtaskId(task.getId());
                        manager.updateEpicStatus(epic);
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }
            // Обновляем счетчик id
            int maxId = 0;
            for (Integer id : manager.tasks.keySet()) {
                if (id > maxId) maxId = id;
            }
            for (Integer id : manager.epics.keySet()) {
                if (id > maxId) maxId = id;
            }
            for (Integer id : manager.subtasks.keySet()) {
                if (id > maxId) maxId = id;
            }
            manager.id = maxId;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
        return manager;
    }

    // Преобразование задачи в строку CSV
    private String toString(Task task) {
        // Определяем тип задачи
        String type;
        if (task instanceof Epic) {
            type = "EPIC";
        } else if (task instanceof Subtask) {
            type = "SUBTASK";
        } else {
            type = "TASK";
        }
        // Формируем базовые поля
        String csv = String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getName(),
                task.getStatus().toString(),
                task.getDescription() != null ? task.getDescription() : ""
        );
        // Добавляем epicId для подзадач
        if (task instanceof Subtask) {
            csv += "," + ((Subtask) task).getEpicId();
        } else {
            csv += ","; // Добавляем пустое поле для эпика
        }
        return csv;
    }

    // Преобразование строки CSV в объект задачи
    private static Task fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ManagerSaveException("Строка не может быть пустой");
        }

        String[] values = value.split(",", -1);
        if (values.length < 6) {
            throw new ManagerSaveException("Недостаточно данных в строке");
        }

        try {
            String id = values[0].trim();
            String type = values[1].trim();
            String name = values[2].trim();
            String taskStatus = values[3].trim();
            String description = values[4].trim();

            switch (type) {
                case "EPIC":
                    Epic epic = new Epic(name, description);
                    epic.setId(Integer.parseInt(id));
                    epic.setStatus(Status.valueOf(taskStatus.toUpperCase()));
                    return epic;
                case "SUBTASK":
                    if (values.length < 6 || values[5].trim().isEmpty()) {
                        throw new ManagerSaveException("Для подзадачи не указан ID эпика");
                    }
                    Integer idOfEpic = Integer.valueOf(values[5].trim());
                    Subtask subtask = new Subtask(name, description, idOfEpic);
                    subtask.setId(Integer.parseInt(id));
                    subtask.setStatus(Status.valueOf(taskStatus.toUpperCase()));
                    return subtask;
                case "TASK":
                    Task task = new Task(name, description);
                    task.setId(Integer.parseInt(id));
                    task.setStatus(Status.valueOf(taskStatus.toUpperCase()));
                    return task;
                default:
                    throw new ManagerSaveException("Неизвестный тип задачи: " + type);
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Ошибка формата числа", e);
        } catch (IllegalArgumentException e) {
            throw new ManagerSaveException("Некорректное значение статуса", e);
        }
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
        super.removeSubtaskById(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }
}
