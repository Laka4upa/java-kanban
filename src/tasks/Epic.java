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
        this.subIds = new ArrayList<>(subIds);
    }

    public List<Integer> getSubIds() {
        return new ArrayList<>(subIds);
    }

    public boolean addSubtaskId(int subtaskId) {
        if (subIds.contains(subtaskId)) return false;
        subIds.add(subtaskId);
        return true;
    }

    public boolean removeSubtaskId(int subtaskId) {
       return subIds.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtaskIds() {
        subIds.clear();
    }

    @Override
    public Epic copy() {
    Epic copy = new Epic(this.name, this.description, new ArrayList<>(this.subIds));
    copy.setId(this.id);
    copy.setStatus(this.status);
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
                "id = '" + getId() + '\'' +
                ", Name = '" + super.getName() + '\'' +
                ", Status = '" + getStatus() + '\'' +
                ", Description = '" + getDescription() + '\'' +
                ", subIds= " + subIds +
                '}';
    }
}