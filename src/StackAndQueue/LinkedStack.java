package StackAndQueue;

import StackAndQueue.Exceptions.EmptyStackException;

public class LinkedStack<T> implements Stack<T> {
    private class node {
        private T element;
        private node next;

        node(T element, node next) {
            this.element = element;
            this.next = next;
        }

        private T getElement() {
            return element;
        }

        private node getNext() {
            return next;
        }
    }

    private node top;

    public LinkedStack() {
        top = null;
    }

    @Override
    public int getSize() {
        int len = 0;
        node p = top;
        while (p != null) {
            len++;
            p = p.getNext();
        }
        return len;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return top == null;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void push(T element) {
        top = new node(element, top);
    }

    @Override
    public T pop(){
        if (top == null) {
            throw new EmptyStackException();
        } else {
            node p = top;
            top = top.getNext();
            return p.getElement();
        }
    }

    @Override
    public T getTop(){
        if (top == null) {
            throw new EmptyStackException();
        } else {
            return top.getElement();
        }

    }

    @Override
    public void clear() {
        top = null;
    }


}
