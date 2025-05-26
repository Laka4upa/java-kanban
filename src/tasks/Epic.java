package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private final List<Integer> subIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.subIds = new ArrayList<>();
    }

    public Epic(String name, String description, List<Integer> subIds) {
        super(name, description);
        this.subIds = new ArrayList<>(subIds);
    }

    public List<Integer> getSubIds() {
        return new ArrayList<>(subIds);
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean addSubtaskId(int subtaskId) {
        if (subIds.contains(subtaskId)) return false;
        subIds.add(subtaskId);
        return true;
    }

    public boolean removeSubtaskById(int subtaskId) {
       return subIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtaskIds() {
        subIds.clear();
    }

    @Override
    public String getType() {
        return "EPIC";
    }

    // Метод для обновления временных параметров эпика на основе подзадач
    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.duration = null;
            this.startTime = null;
            this.endTime = null;
            return;
        }
        // Находим самую раннюю startTime
        Optional<LocalDateTime> minStartTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
        // Находим самую позднюю endTime
        Optional<LocalDateTime> maxEndTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        // Суммируем продолжительности всех подзадач
        long totalMinutes = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        // Устанавливаем параметры
        this.startTime = minStartTime.orElse(null);
        this.endTime = maxEndTime.orElse(null);
        this.duration = totalMinutes > 0 ? Duration.ofMinutes(totalMinutes) : null;
    }

    @Override
    public String toCsv() {
        return String.join(",",
                String.valueOf(id),
                getType(),
                escapeCsvField(name),
                status.toString(),
                escapeCsvField(description),
                "", // Пустое поле для эпика
                duration == null ? "" : String.valueOf(duration.toMinutes()),
                startTime == null ? "" : startTime.toString(),
                endTime == null ? "" : endTime.toString()
        );
    }

    @Override
    public Epic copy() {
        Epic copy = new Epic(this.name, this.description, new ArrayList<>(this.subIds));
        copy.setId(this.id);
        copy.setStatus(this.status);
        copy.duration = this.duration;
        copy.startTime = this.startTime;
        copy.endTime = this.endTime;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "\nEpic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subIds=" + subIds +
                ", duration=" + (duration == null ? "null" : duration.toMinutes() + "m") +
                ", startTime=" + (startTime == null ? "null" : startTime) +
                ", endTime=" + getEndTime() +
                '}';
    }
}