package List;

import List.Exceptions.IlleagleListCapacityException;
import List.Exceptions.IndexOutOfBoundsException;
import List.Exceptions.ListOverflowException;

import java.util.Iterator;

public class StaticLinkedList<T> implements List<T> {

    private class node {
        private T data;
        private int next;

        private node(T data, int next) {
            this.data = data;
            this.next = next;
        }

        private T getData() {
            return data;
        }

        private int getNext() {
            return next;
        }

        private void setData(T data) {
            this.data = data;
        }

        private void setNext(int next) {
            this.next = next;
        }
    }

    private node[] store;

    private int head, free;
    private int capacity;

    private static final int END = -1;

    @SuppressWarnings("unchecked")
    public StaticLinkedList(int capacity) {
        if (capacity <= 0) {
            throw new IlleagleListCapacityException();
        }
        this.capacity = capacity;
        head = END;
        free = 0;
        store = (node[]) new Object[capacity];
        for (int i = 0; i < capacity - 1; i++) {
            store[i] = new node(null, i + 1);
        }
        store[capacity - 1] = new node(null, END);
    }

    private int getFree() {  //获得空节点
        if (free == END) {
            throw new ListOverflowException();
        }
        int ret = free;
        free = store[free].getNext();
        return ret;
    }

    private void recycle(int position) {
        store[position].setNext(free);
        free = position;
    }

    @Override
    public void insert(T element, int index) {
        if (index >= 0) {
            if (index == 0) {  //index==0先处理
                int newNode = getFree();
                store[newNode].setData(element);
                store[newNode].setNext(head);
                head = newNode;
                return;
            }

            int now = head;
            int count = 0;
            while (now != END) {  //index最小为1
                if (count + 1 == index) {
                    int newNode = getFree();
                    store[newNode].setData(element);
                    store[newNode].setNext(store[now].getNext());
                    store[now].setNext(newNode);
                    return;
                }
                now = store[now].getNext();
                count++;
            }

        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public void delete(int index) {
        if (index >= 0 && head != END) {

            if (index == 0) {
                int tmp = head;
                head = store[head].getNext();
                recycle(tmp);
                return;
            }

            int now = head;
            int count = 0;
            while (now != END) {
                if (count + 1 == index) {
                    int next = store[now].getNext();
                    if (next == END) {
                        break;
                    }
                    store[now].setNext(store[next].getNext());
                    recycle(next);
                    return;
                }
                count++;
                now = store[now].getNext();
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public T get(int index) {
        if (index >= 0) {
            int now = head;
            int count = 0;
            while (now != END) {
                if (count == index) {
                    return store[now].getData();
                }
                count++;
                now = store[now].getNext();
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public void set(T element, int index) {
        if (index >= 0) {
            int now = head;
            int count = 0;
            while (now != END) {

                if (count == index) {
                    store[now].setData(element);
                    return;
                }
                count++;
                now = store[now].getNext();
            }
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public void clear() {
        head = END;
        free = 0;
        for (int i = 0; i < capacity - 1; i++) {
            store[i].setNext(i + 1);
        }
        store[capacity - 1].setNext(END);
    }

    @Override
    public boolean isFull() {
        return free == END;
    }

    @Override
    public boolean isEmpty() {
        return head == END;
    }

    @Override
    public int getLength() {
        int now = head;
        int len = 0;
        while (now != END) {
            len++;
            now = store[now].getNext();
        }
        return len;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Iterator iterator() {
        return null;
    }
}
