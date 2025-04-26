package managers;

import java.util.*;
import tasks.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList history = new CustomLinkedList();

    @Override
    public void add(Task task) {
        if (task != null) {
            history.linkLast(task);
        }
    }

    @Override
    public void remove(int id) {
        history.removeNode(id);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    private static final class CustomLinkedList {
        private final Map<Integer, Node> nodeMap = new HashMap<>();
        private Node head;
        private Node tail;

        private void linkLast(Task task) {
            if (task == null) {
                return;
            }

            removeNode(task.getId()); // Удаляем существующую задачу, если она есть
            Node newNode = new Node(tail, task, null);

            if (tail == null) {
                head = newNode;
            } else {
                tail.next = newNode;
            }
            tail = newNode;

            nodeMap.put(task.getId(), newNode);
        }

        private List<Task> getTasks() {
            List<Task> tasks = new ArrayList<>();
            Node current = head;
            while (current != null) {
                tasks.add(current.task);
                current = current.next;
            }
            return tasks;
        }

        private void removeNode(int id) {
            Node node = nodeMap.get(id);
            if (node != null) {
                removeNode(node);
            }
        }

        private void removeNode(Node node) {
            if (node == null) {
                return;
            }

            if (node.prev != null) { // Обновляем связи соседних узлов
                node.prev.next = node.next;
            } else {
                head = node.next;
            }

            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                tail = node.prev;
            }

            node.prev = null; // Очищаем ссылки
            node.next = null;

            nodeMap.remove(node.task.getId());
        }
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}
