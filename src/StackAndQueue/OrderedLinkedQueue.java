package StackAndQueue;

import java.util.Comparator;

public class OrderedLinkedQueue<T> extends LinkedQueue<T> {
    private Comparator<T> comparator;

    public OrderedLinkedQueue(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    @Override
    public void add(T element) {
        if (front != null
                && comparator.compare(front.getElement(), element) >= 0) {
            Node n = new Node(element, front);
            front
        }
        Node p = front;
        while (p != null) {//升序
            if (comparator.compare())
        }
    }
}
