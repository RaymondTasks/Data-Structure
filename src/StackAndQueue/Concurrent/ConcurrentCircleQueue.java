package StackAndQueue.Concurrent;

import StackAndQueue.CircleQueue;
import StackAndQueue.Exceptions.EmptyQueueException;
import StackAndQueue.Exceptions.QueueOverflowException;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentCircleQueue<T> extends CircleQueue<T> {

    private ReentrantReadWriteLock.ReadLock readLock;
    private ReentrantReadWriteLock.WriteLock writeLock;

    @SuppressWarnings("unchecked")
    public ConcurrentCircleQueue(int capacity) {
        super(capacity);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public int getSize() {
        readLock.lock();
        int size = (rear - front) % max;
        readLock.unlock();
        return size;
    }

    @Override
    public int getCapacity() {
        readLock.lock();
        int max2 = max;
        readLock.unlock();
        return max2 - 1;
    }

    @Override
    public boolean isEmpty() {
        readLock.lock();
        boolean result = front == rear;
        readLock.unlock();
        return result;
    }

    @Override
    public boolean isFull() {
        readLock.lock();
        boolean result = (rear + 1) % max == front;
        readLock.unlock();
        return result;
    }

    @Override
    public T getTail(){
        T ret;
        readLock.lock();
        if (front == rear) {
            readLock.unlock();
            throw new EmptyQueueException();
        } else {
            ret = queue[rear];
        }
        readLock.unlock();
        return ret;
    }

    @Override
    public T getHead() {
        T ret;
        readLock.lock();
        if (front == rear) {
            readLock.unlock();
            throw new EmptyQueueException();
        } else {
            ret = queue[front];
        }
        readLock.unlock();
        return ret;
    }

    @Override
    public void add(T element){
        writeLock.lock();
        if ((rear + 1) % max == front) {
            writeLock.unlock();
            throw new QueueOverflowException();
        } else {
            queue[rear] = element;
            rear = (rear + 1) % max;
        }
        writeLock.unlock();
    }

    @Override
    public T get(){
        T ret;
        writeLock.lock();
        if (front == rear) {
            writeLock.unlock();
            throw new EmptyQueueException();
        } else {
            ret = queue[front];
            front = (front + 1) % max;
        }
        writeLock.unlock();
        return ret;
    }

    @Override
    public void clear() {
        writeLock.lock();
        front = 0;
        rear = 0;
        writeLock.unlock();
    }
}
