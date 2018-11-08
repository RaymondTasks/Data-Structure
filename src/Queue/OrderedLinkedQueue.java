package Queue;

import java.util.Comparator;

public class OrderedLinkedQueue<T> extends LinkedQueue<T> {
    private Comparator<T> comparator;

    public OrderedLinkedQueue(Comparator<T> comparator) {
        super();
        this.comparator = comparator;
    }

    @Override
    public void add(T element) {
        var p = store.getHeadNode();
        while (p.getNext() != null) {
            if (comparator.compare(element, p.getNext().getData()) > 0) {
                p = p.getNext();
            } else {
                p.setNext(store.getNewNode(element, p.getNext()));
                return;
            }
        }
        store.insertTail(element);
    }
}
