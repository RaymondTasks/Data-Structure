package Stack;

import Stack.Exceptions.StackOverflowException;

import java.util.EmptyStackException;

public class SequentStack<T> implements Stack<T> {

    private int top;
    private int capacity;
    private Object[] store;

    public SequentStack(int capacity) {
        top = -1;
        this.capacity = capacity;
        store = new Object[this.capacity];
    }

    @Override
    public int getCapacity() {
        return capacity;
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
        return top == capacity - 1;
    }

    @Override
    public void push(T element) {
        if (top == capacity - 1) {
            throw new StackOverflowException();
        } else {
            store[++top] = element;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T pop() {
        if (top == -1) {
            throw new EmptyStackException();
        } else {
            return (T) store[top--];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getTop() {
        if (top == -1) {
            throw new EmptyStackException();
        } else {
            return (T) store[top];
        }
    }

    @Override
    public void clear() {
        top = -1;
    }

}
