package StackAndQueue;

import StackAndQueue.Exceptions.EmptyQueueException;
import StackAndQueue.Exceptions.QueueOverflowException;

public interface Queue<T> {
    public int getSize();

    public int getCapacity();

    public boolean isEmpty();

    public boolean isFull();

    public void add(T element);

    public T get() ;

    public T getHead();

    public T getTail() ;

    public void clear();
}
