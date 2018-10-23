package Queue;

import Queue.Exception.EmptyQueueException;
import Queue.Exception.QueueOverflowException;

public class CircleQueue<T> implements Queue<T> {

    private int max, front, rear;
    private Object[] store;

    public CircleQueue(int capacity) {
        max = capacity + 1;
        front = 0;
        rear = 0;
        store = new Object[max];
    }

    @Override
    public int getSize() {
        return (rear - front) % max;
    }

    @Override
    public int getCapacity() {
        return max - 1;
    }

    @Override
    public boolean isEmpty() {
        return front == rear;
    }

    @Override
    public boolean isFull() {
        return (rear + 1) % max == front;
    }


    @Override
    @SuppressWarnings("unchecked")
    public T getTail() {
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            return (T) store[rear];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getHead() {
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            return (T) store[front];
        }
    }

    @Override
    public void add(T element) {
        if ((rear + 1) % max == front) {
            throw new QueueOverflowException();
        } else {
            store[rear] = element;
            rear = (rear + 1) % max;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            var p = (T) store[front];
            front = (front + 1) % max;
            return p;
        }
    }

    @Override
    public void clear() {
        front = 0;
        rear = 0;
    }

}
