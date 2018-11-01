package Queue;


import List.LinkedList;
import Queue.Exception.EmptyQueueException;

public class LinkedQueue<T> implements Queue<T> {

    protected LinkedList<T> store;

    public LinkedQueue() {
        store = new LinkedList<T>();
    }

    @Override
    public int getSize() {
        return store.getLength();
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public T getTail() {
        if (!store.isEmpty()) {
            return store.getTail();
        }
        throw new EmptyQueueException();
    }

    @Override
    public T getHead() {
        if (!store.isEmpty()) {
            return store.getHead();
        }
        throw new EmptyQueueException();
    }

    @Override
    public void add(T element) {
        store.insertTail(element);
    }

    @Override
    public T get() {
        if (!store.isEmpty()) {
            var p = store.getHead();
            store.delete(0);
            return p;
        }
        throw new EmptyQueueException();
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public Iterator<T> iterator() {
        var iter = store.iterator();
        return new Iterator<T>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return iter.next();
            }

            @Override
            public void reset() {
                iter.reset();
            }
        };
    }
}