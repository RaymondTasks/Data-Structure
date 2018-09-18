package StackAndQueue;

import StackAndQueue.Exceptions.EmptyStackException;
import StackAndQueue.Exceptions.StackOverflowException;

public interface Stack<T> {

    public int getSize();

    public int getCapacity();

    public boolean isEmpty();

    public boolean isFull();

    public void push(T element);

    public T pop();

    public T getTop();

    public void clear();

}
