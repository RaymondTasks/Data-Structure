package StackAndQueue.Concurrent;

import StackAndQueue.Exceptions.EmptyStackException;
import StackAndQueue.Exceptions.StackOverflowException;
import StackAndQueue.SequentStack;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentSequentStack<T> extends SequentStack<T> {

    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;


    @SuppressWarnings("unchecked")
    public ConcurrentSequentStack(int max) {
        super(max);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public int getSize() {
        readLock.lock();
        int top2 = top;
        readLock.unlock();
        return top2 + 1;
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        int top2 = top;
        readLock.unlock();
        return top2 == -1;
    }

    @Override
    public boolean isFull() {
        readLock.lock();
        int top2 = top;
        readLock.unlock();
        return top2 == max - 1;
    }

    @Override
    public void push(T element) {
        writeLock.lock();
        if (top == max - 1) {
            writeLock.unlock();
            throw new StackOverflowException();
        } else {
            stack[++top] = element;
        }
        writeLock.unlock();
    }

    @Override
    public T pop() {
        T ret;
        writeLock.lock();
        if (top == -1) {
            writeLock.unlock();
            throw new EmptyStackException();
        } else {
            ret = stack[top--];
        }
        writeLock.unlock();
        return ret;
    }

    @Override
    public T getTop() {
        T ret;
        writeLock.lock();
        if (top == -1) {
            writeLock.unlock();
            throw new EmptyStackException();
        } else {
            ret = stack[top];
        }
        writeLock.unlock();
        return ret;

    }

    @Override
    public void clear() {
        writeLock.lock();
        top = -1;
        writeLock.unlock();
    }
}
