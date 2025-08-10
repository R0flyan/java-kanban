import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        Node existNode = historyMap.get(task.getId());
        if (existNode != null) {
            removeNode(existNode);
        }
        linkLast(task); //добавляем узел с задачей
        historyMap.put(task.getId(), tail);   // обновляем и указываем на новый узел
    }

    @Override
    public void remove(int id) {
        Node node = historyMap.remove(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks(); //возвращаем список задач
    }

    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    private static class Node {
        Task task; //задача в узле
        Node previous; //ссылка на пред. узел
        Node next; //ссылка на след узел

        Node(Task task) {
            this.task = task;
        }
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task);
        if (tail == null) {
            head = newNode; //если список пуст, то появляется голова
        } else {
            tail.next = newNode;
            newNode.previous = tail;
        }
        tail = newNode; //новый узел становится хвостом
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node curr = head;
        while (curr != null) {
            tasks.add(curr.task); //проход по узлам и добавление задачи
            curr = curr.next; //переход к следующему узлу
        }
        return tasks;
    }

    private void removeNode(Node node) {
        if (node.previous != null) {
            node.previous.next = node.next; //предыдущий next ссылается не следующий после удаляемого
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.previous = node.previous; //следующий prev ссылатся на предыдущий перед удаляемого
        } else {
            tail = node.previous;
        }
    }
}
