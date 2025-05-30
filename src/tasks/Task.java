package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import util.Status;
import util.TaskUtils;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;

    // Конструкторы
    public Task(String name, String description) {
        this(name, description, Status.NEW);
    }

    public Task(String name, String description, Status status) {
        this(name, description, 0, status, null, null);
    }

    public Task(String name, String description, int id, Status status) {
        this(name, description, id, status, null, null);
    }

    public Task(String name, String description, Status status, Duration duration, LocalDateTime startTime) {
        this(name, description, 0, status, duration, startTime);
    }

    public Task(String name, String description, int id, Status status, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    // Методы для работы с задачами
    public String getType() {
        return "TASK";
    }

    public Task copy() {
        return TaskUtils.copyTask(this);
    }

    public String toCsv() {
        return TaskUtils.toCsv(this);
    }

    // Переопределенные методы Object
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration == null ? "null" : duration.toMinutes() + "m") +
                ", startTime=" + (startTime == null ? "null" : startTime) +
                ", endTime=" + getEndTime() +
                '}';
    }
}