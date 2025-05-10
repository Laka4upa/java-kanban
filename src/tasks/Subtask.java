package tasks;

import util.Status;

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

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Subtask copy() {
        return new Subtask(this.name, this.description, this.id, this.status, this.epicId);
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
    public String getType() {
        return "SUBTASK";
    }

    @Override
    public String toCsv() {
        return String.join(",",
                String.valueOf(id),
                getType(),
                escapeCsvField(name),
                status.toString(),
                escapeCsvField(description),
                String.valueOf(epicId)
        );
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
