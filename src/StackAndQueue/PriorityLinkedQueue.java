package StackAndQueue;

import StackAndQueue.Exceptions.EmptyQueuePriorityException;
import StackAndQueue.Exceptions.IlleaglePriorityException;
import StackAndQueue.Exceptions.EmptyQueueException;

public class PriorityLinkedQueue<T> extends LinkedQueue<T> {

    private int priorityLevel;
    private LinkedQueue[] queue;

    public PriorityLinkedQueue(int priorityLevel) {
        this.priorityLevel = priorityLevel;
        queue = new LinkedQueue[priorityLevel];
        for (int i = 0; i < priorityLevel; i++) {
            queue[i] = new LinkedQueue<T>();
        }
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < priorityLevel; i++) {
            if (!queue[i].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (int i = 0; i < priorityLevel; i++) {
            size += queue[i].getSize();
        }
        return size;
    }

    @Override
    public T getTail() {
        for (int i = 0; i < priorityLevel; i++) {
            if (!queue[i].isEmpty()) {
                T ret;
                try {
                    ret = (T) queue[i].getTail();
                } catch (EmptyQueueException e) {
                    continue;
                }
                return ret;
            }
        }
        throw new EmptyQueueException();
    }

    public T getTail(int priority) {
        if (priority < 0 || priority >= priorityLevel) {
            throw new IlleaglePriorityException();
        } else {
            T ret;
            try {
                ret = (T) queue[priority].getTail();
            } catch (EmptyQueueException e) {
                throw new EmptyQueuePriorityException();
            }
            return ret;
        }
    }

    @Override
    public T getHead() {
        for (int i = priorityLevel - 1; i >= 0; i--) {
            if (!queue[i].isEmpty()) {
                T ret;
                try {
                    ret = (T) queue[i].getHead();
                } catch (EmptyQueueException e) {
                    continue;
                }
                return ret;
            }
        }
        throw new EmptyQueueException();
    }

    public T getHead(int priority) {
        if (priority < 0 || priority >= priorityLevel) {
            throw new IlleaglePriorityException();
        } else {
            T ret;
            try {
                ret = (T) queue[priority].getHead();
            } catch (EmptyQueueException e) {
                throw new EmptyQueuePriorityException();
            }
            return ret;
        }
    }

    @Override
    public void add(T element) {
        queue[0].add(element);
    }

    public void add(T element, int priority) {
        if (priority < 0 || priority >= priorityLevel) {
            throw new IlleaglePriorityException();
        } else {
            queue[priority].add(element);
        }
    }

    @Override
    public T get() {
        for (int i = priorityLevel - 1; i >= 0; i--) {
            if (!queue[i].isEmpty()) {
                T ret;
                try {
                    ret = (T) queue[i].get();
                } catch (EmptyQueueException e) {
                    continue;
                }
            }
        }
        throw new EmptyQueueException();
    }

    public T get(int priority) {
        if (priority < 0 || priority >= priorityLevel) {
            throw new IlleaglePriorityException();
        } else {
            if (queue[priority].isEmpty()) {
                throw new EmptyQueuePriorityException();
            } else {
                T ret;
                try {
                    ret = (T) queue[priority].get();
                } catch (EmptyQueueException e) {
                    throw new EmptyQueuePriorityException();
                }
                return ret;
            }
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < priorityLevel; i++) {
            queue[i].clear();
        }
    }

    public void clear(int priority) {
        if (priority < 0 || priority >= priorityLevel) {
            throw new IlleaglePriorityException();
        } else {
            queue[priority].clear();
        }
    }
}
