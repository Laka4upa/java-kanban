package tasks;

import util.TaskUtils;
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

    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.duration = null;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        Optional<LocalDateTime> minStartTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        Optional<LocalDateTime> maxEndTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        long totalMinutes = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();

        this.startTime = minStartTime.orElse(null);
        this.endTime = maxEndTime.orElse(null);
        this.duration = totalMinutes > 0 ? Duration.ofMinutes(totalMinutes) : null;
    }

    @Override
    public String toCsv() {
        String subTasksIds = String.join(";", subIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new));
        return TaskUtils.toCsv(this, subTasksIds);
    }

    @Override
    public Epic copy() {
        return TaskUtils.copyEpic(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subIds, epic.subIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subIds);
    }

    @Override
    public String toString() {
        return "Epic{" +
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