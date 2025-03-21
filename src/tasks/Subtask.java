package tasks;

import java.util.Objects;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String nameTask, String description, int epicId) {
        super(nameTask, description);
        super.setStatus(TaskStatus.NEW);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int id, TaskStatus status, int epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
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
        return "\nSubtask{" +
                "id = '" + getId() + '\'' +
                ", Name = '" + getName() + '\'' +
                ", Status = '" + getStatus() + '\'' +
                ", Description = '" + getDescription() + '\'' +
                ", epicId = " + epicId +
                "}";
    }
}
