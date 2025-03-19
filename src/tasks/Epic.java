package tasks;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private final ArrayList<Integer> subIds;

    public Epic(String name, String description) {
        super(name, description);
        this.subIds = new ArrayList<>();
    }

    public Epic(String name, String description,int id, ArrayList<Integer> subIds) {
        super(id, name, description);
        this.subIds = subIds;
    }

    public ArrayList<Integer> getSubIds() {
        return subIds;
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
        return "\nEpic{" +
                "id = '" + getId() + '\'' +
                ", Name = '" + super.getName() + '\'' +
                ", Status = '" + getStatus() + '\'' +
                ", Description = '" + getDescription() + '\'' +
                ", subIds= " + subIds +
                '}';
    }
}