package Stack;


import List.LinkedList;

import java.util.EmptyStackException;

public class LinkedStack<T> implements Stack<T> {

    //通过链表实现

    private LinkedList<T> store;

    public LinkedStack() {
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
    public void push(T element) {
        store.insert(element, 0);
    }

    @Override
    public T pop() {
        if (store.isEmpty()) {
            throw new EmptyStackException();
        } else {
            var p = store.get(0);
            store.delete(0);
            return p;
        }
    }

    @Override
    public T getTop() {
        if (store.isEmpty()) {
            throw new EmptyStackException();
        } else {
            return store.get(0);
        }
    }

    @Override
    public void clear() {
        store.clear();
    }
}
