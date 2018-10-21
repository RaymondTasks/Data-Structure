package StackAndQueue;

import StackAndQueue.Exceptions.EmptyQueueException;

public class LinkedQueue<T> implements Queue<T> {

    protected class Node {
        private T element;
        private Node next;

        public T getElement() {
            return element;
        }

        public Node getNext() {
            return next;
        }

        public void setNext(Node next) {
            this.next = next;
        }

        public Node(T element, Node next) {
            this.element = element;
            this.next = next;
        }
    }

    protected Node front, rear;

    public LinkedQueue() {
        front = null;
        rear = null;
    }

    @Override
    public int getSize() {
        int len = 0;
        Node p = front;
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
        return front == null || rear == null;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public T getTail(){
        if (rear == null) {
            throw new EmptyQueueException();
        } else {
            return rear.getElement();
        }
    }

    @Override
    public T getHead(){
        if (front == null) {
            throw new EmptyQueueException();
        } else {
            return front.getElement();
        }
    }

    @Override
    public void add(T element) {
        Node p = new Node(element, null);
        if (front == null || rear == null) {
            front = p;
            rear = p;
        } else {
            rear.setNext(p);
            rear = p;
        }
    }

    @Override
    public T get() {
        if (front == null || rear == null) {
            throw new EmptyQueueException();
        } else {
            Node p = front;
            front = front.getNext();
            if (front == null) {
                rear = null;
            }
            return p.getElement();
        }
    }

    @Override
    public void clear() {
        front = null;
        rear = null;
    }
}