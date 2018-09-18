package StackAndQueue.Concurrent;

import StackAndQueue.Exceptions.EmptyQueueException;
import StackAndQueue.LinkedQueue;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentLinkedQueue<T> extends LinkedQueue<T> {

    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    private class node {
        private T element;
        private node next;

        private T getElement() {
            return element;
        }

        private node getNext() {
            return next;
        }

        private void setNext(node next) {
            this.next = next;
        }

        node(T element, node next) {
            this.element = element;
            this.next = next;
        }
    }

    private node front, rear;

    public ConcurrentLinkedQueue() {
        front = null;
        rear = null;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public int getSize() {
        int len = 0;
        readLock.lock();
        node p = front;
        while (p != null) {
            len++;
            p = p.getNext();
        }
        readLock.unlock();
        return len;
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        boolean result = front == null || rear == null;
        readLock.unlock();
        return result;
    }

    @Override
    public T getTail() {
        readLock.lock();
        node rear2 = rear;
        readLock.unlock();
        if (rear2 == null) {
            throw new EmptyQueueException();
        } else {
            return rear2.getElement();
        }
    }

    @Override
    public T getHead(){
        readLock.lock();
        node front2 = front;
        readLock.unlock();
        if (front2 == null) {
            throw new EmptyQueueException();
        } else {
            return front2.getElement();
        }
    }

    @Override
    public void add(T element) {
        node p = new node(element, null);
        writeLock.lock();
        if (front == null || rear == null) {
            front = p;
            rear = p;
        } else {
            rear.setNext(p);
            rear = p;
        }
        writeLock.unlock();
    }

    @Override
    public T get() {
        node p;
        writeLock.lock();
        if (front == null || rear == null) {
            writeLock.unlock();
            throw new EmptyQueueException();
        } else {
            p = front;
            front = front.getNext();
            if (front == null) {
                rear = null;
            }
        }
        writeLock.unlock();
        return p.getElement();
    }

    @Override
    public void clear() {
        writeLock.lock();
        front = null;
        rear = null;
        writeLock.unlock();
    }
}
