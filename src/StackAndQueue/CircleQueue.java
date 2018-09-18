package StackAndQueue;

import StackAndQueue.Exceptions.EmptyQueueException;
import StackAndQueue.Exceptions.QueueOverflowException;

public class CircleQueue<T> implements Queue<T> {
    protected int max, front = 0, rear = 0;

    protected T[] queue;

    @SuppressWarnings("unchecked")
    public CircleQueue(int capacity) {
        max = capacity + 1;
        queue = (T[]) new Object[max];
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
    public T getTail(){
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            return queue[rear];
        }
    }

    @Override
    public T getHead(){
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            return queue[front];
        }
    }

    @Override
    public void add(T element) {
        if ((rear + 1) % max == front) {
            throw new QueueOverflowException();
        } else {
            queue[rear] = element;
            rear = (rear + 1) % max;
        }
    }

    @Override
    public T get() {
        if (front == rear) {
            throw new EmptyQueueException();
        } else {
            T p = queue[front];
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
