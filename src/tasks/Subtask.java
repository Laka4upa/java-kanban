package tasks;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        super.setStatus(TaskStatus.NEW);
        this.epicId = epicId;
    }

    public Subtask(String nameTask, String description, int id, TaskStatus status, int epicId) {
        super(nameTask, description, id, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
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
        return "\nSubtask{" +
                "id = '" + getId() + '\'' +
                ", Name = '" + getName() + '\'' +
                ", Status = '" + getStatus() + '\'' +
                ", Description = '" + getDescription() + '\'' +
                ", epicId = " + epicId +
                "}";
    }
}
