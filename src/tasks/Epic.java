package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subIds;

    public Epic(String name, String description) {
        super(name, description);
        this.subIds = new ArrayList<>();
    }

    public Epic(String name, String description, List<Integer> subIds) {
        super(name, description);
        this.subIds = subIds;
    }

    public List<Integer> getSubIds() {
        return new ArrayList<>(subIds);
    }

    public void addSubtaskId(int subtaskId) {
        if (!subIds.contains(subtaskId)) {
            subIds.add(subtaskId);
        }
    }

    public void removeSubtaskId(int subtaskId) {
        subIds.remove(Integer.valueOf(subtaskId));
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
                "id = '" + getId() + '\'' +
                ", Name = '" + super.getName() + '\'' +
                ", Status = '" + getStatus() + '\'' +
                ", Description = '" + getDescription() + '\'' +
                ", subIds= " + subIds +
                '}';
    }
}