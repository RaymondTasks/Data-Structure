package StackAndQueue.Concurrent;

import StackAndQueue.Exceptions.EmptyStackException;
import StackAndQueue.LinkedStack;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentLinkedStack<T> extends LinkedStack<T> {

    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

//    private class node {
//        private T element;
//        private node next;
//
//        node(T element, node next) {
//            this.element = element;
//            this.next = next;
//        }
//
//        private T getElement() {
//            return element;
//        }
//
//        private node getNext() {
//            return next;
//        }
//    }

    private node top;

    public ConcurrentLinkedStack() {
        top = null;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public int getSize() {
        int len = 0;
        readLock.lock();
        node p = top;
        while (p != null) {
            len++;
            p = p.getNext();
        }
        readLock.lock();
        return len;
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        boolean result = top == null;
        readLock.unlock();
        return result;
    }

    @Override
    public void push(T element) {
        writeLock.lock();
        top = new node(element, top);
        writeLock.lock();
    }

    @Override
    public T pop(){
        node p;
        writeLock.lock();
        if (top == null) {
            writeLock.unlock();
            throw new EmptyStackException();
        } else {
            p = top;
            top = top.getNext();
        }
        writeLock.unlock();
        return p.getElement();
    }

    @Override
    public T getTop(){
        readLock.lock();
        node top2 = top;
        readLock.unlock();
        if (top2 == null) {
            throw new EmptyStackException();
        } else {
            return top2.getElement();
        }
    }

    @Override
    public void clear() {
        writeLock.lock();
        top = null;
        writeLock.unlock();
    }

}
