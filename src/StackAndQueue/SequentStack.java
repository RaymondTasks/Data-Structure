package StackAndQueue;

import StackAndQueue.Exceptions.EmptyStackException;
import StackAndQueue.Exceptions.StackOverflowException;


public class SequentStack<T> implements Stack<T> {

    protected int top;
    protected int max;
    protected T[] stack;

    @SuppressWarnings("unchecked")
    public SequentStack(int max) {
        top = -1;
        this.max = max;
        stack = (T[]) new Object[max];
    }

    @Override
    public int getCapacity() {
        return max;
    }

    @Override
    public int getSize() {
        return top + 1;
    }

    @Override
    public boolean isEmpty() {
        return top == -1;
    }

    @Override
    public boolean isFull() {
        return top == max - 1;
    }

    @Override
    public void push(T element) {
        if (top == max - 1) {
            throw new StackOverflowException();
        } else {
            stack[++top] = element;
        }
    }

    @Override
    public T pop() {
        if (top == -1) {
            throw new EmptyStackException();
        } else {
            return stack[top--];
        }
    }

    @Override
    public T getTop() {
        if (top == -1) {
            throw new EmptyStackException();
        } else {
            return stack[top];
        }
    }

    @Override
    public void clear() {
        top = -1;
    }

}
