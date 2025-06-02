package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import util.Status;
import util.TaskUtils;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        super.setStatus(Status.NEW);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, Status status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, Status status, int epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String getType() {
        return "SUBTASK";
    }

    @Override
    public String toCsv() {
        return TaskUtils.toCsv(this, String.valueOf(epicId));
    }

    @Override
    public Subtask copy() {
        return TaskUtils.copySubtask(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                ", duration=" + (duration == null ? "null" : duration.toMinutes() + "m") +
                ", startTime=" + (startTime == null ? "null" : startTime) +
                ", endTime=" + getEndTime() +
                '}';
    }
}