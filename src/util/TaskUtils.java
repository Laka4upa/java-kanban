package util;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;

public class TaskUtils {
    public static String toCsv(Task task, String additionalField) {
        return String.join(",",
                String.valueOf(task.getId()),
                task.getType(),
                escapeCsvField(task.getName()),
                task.getStatus().toString(),
                escapeCsvField(task.getDescription()),
                additionalField,
                task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes()),
                task.getStartTime() == null ? "" : task.getStartTime().toString()
        );
    }

    public static String toCsv(Task task) {
        return toCsv(task, "");
    }

    public static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    public static Task copyTask(Task original) {
        Task copy = new Task(
                original.getName(),
                original.getDescription(),
                original.getId(),
                original.getStatus(),
                original.getDuration(),
                original.getStartTime()
        );
        return copy;
    }

    public static Subtask copySubtask(Subtask original) {
        Subtask copy = new Subtask(
                original.getName(),
                original.getDescription(),
                original.getId(),
                original.getStatus(),
                original.getEpicId(),
                original.getDuration(),
                original.getStartTime()
        );
        return copy;
    }

    public static Epic copyEpic(Epic original) {
        Epic copy = new Epic(
                original.getName(),
                original.getDescription(),
                new ArrayList<>(original.getSubIds())
        );
        copy.setId(original.getId());
        copy.setStatus(original.getStatus());
        copy.setDuration(original.getDuration());
        copy.setStartTime(original.getStartTime());
        return copy;
    }
}