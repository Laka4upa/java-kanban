package managers;

import tasks.*;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        int maxHistorySize = 10;
        if (history.size() == maxHistorySize) history.removeFirst();
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
