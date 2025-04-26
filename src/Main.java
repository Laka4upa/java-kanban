import managers.*;
import tasks.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Проснуться");
        Task task2 = new Task("Задача 2", "Встрепенуться");
        Task task3 = new Task("Задача 3", "Заниматься");
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addTask(task3);

        Epic epic1 = new Epic("ЭПИК 1", "Работать не покладая крюк");
        taskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Сабтаск 1", "Захватить корабль", epic1.getId());
        Subtask subtask2 = new Subtask("Сабтаск 2", "Поднять паруса", epic1.getId());
        Subtask subtask3 = new Subtask("Сабтаск 3", "Ярррргггххх!!!", epic1.getId());
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        Epic epic2 = new Epic("ЭПИК 2", "Поход в магазин");
        taskManager.addEpic(epic2);
        Subtask subtask4 = new Subtask("Сабтаск 4", "Составить список покупок", epic2.getId());
        Subtask subtask5 = new Subtask("Сабтаск 5", "Прибыть и победить!", epic2.getId());
        taskManager.addSubtask(subtask4);
        taskManager.addSubtask(subtask5);

        System.out.println("добавили задач");
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllSubtasksOfEpic(epic1));
        System.out.println(taskManager.getAllSubtasksOfEpic(epic2));

        Task taskToUpdate = taskManager.getTaskById(task1.getId());
        taskToUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskToUpdate);
        taskManager.updateSubtask(new Subtask("Сабтаск 1", "Найти подходящий корабль",
                subtask1.getId(), Status.IN_PROGRESS, subtask1.getEpicId()));
        taskManager.updateSubtask(new Subtask(subtask4.getName(), "Составить список покупок",
                subtask4.getId(), Status.DONE, subtask4.getEpicId()));
        taskManager.updateSubtask(new Subtask(subtask5.getName(), subtask5.getDescription(),
                subtask5.getId(), Status.DONE, subtask5.getEpicId()));
        //При передаче "левых id" старые на месте
        taskManager.updateEpic(new Epic("ЭПИК 1мод", "не работать))", epic2.getSubIds()));

        System.out.println("проверяем историю");
        System.out.println(taskManager.getHistory());

        System.out.println("обновили задачи");
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println(taskManager.getAllSubtasksOfEpic(epic1));
        System.out.println(taskManager.getAllSubtasksOfEpic(epic2));

        taskManager.removeTaskById(task1.getId());
        taskManager.removeEpicById(epic2.getId());
        taskManager.removeSubtaskById(subtask2.getId());
        System.out.println("удалили выборочно задачи");

        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());
        System.out.println("удалили все задачи");
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        taskManager.removeAllSubtasks();
        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());

    }
}

