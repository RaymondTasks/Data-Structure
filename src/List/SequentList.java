package List;

import java.util.Arrays;

public class SequentList<T> implements List<T> {

    private Object[] store;

    private int length, initialCapacity, increaseStep;

    public SequentList(int initialCapacity, int increaseStep) {
        store = new Object[initialCapacity];
        length = 0;
        this.initialCapacity = initialCapacity;
        this.increaseStep = increaseStep;
    }

    @Override
    public void insert(T element, int index) {
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }
        if (length == store.length) {  //store已满
            var dest = new Object[length + increaseStep];
            System.arraycopy(store, 0, dest, 0, index);
            System.arraycopy(store, index, dest, index + 1, length - index);
            store = dest;
        } else {
            for (int i = store.length - 1; i >= index; i--) {
                store[i + 1] = store[i];
            }
        }
        store[index] = element;
        length++;
    }

//    @Override
//    public void insertHead(T element) {
//        if (length == store.length) {
//            var dest = new Object[length + increaseStep];
//            System.arraycopy(store, 0, dest, 1, length);
//            store = dest;
//        } else {
//            for (int i = store.length - 1; i >= 0; i--) {
//                store[i + 1] = store[i];
//            }
//        }
//        store[0] = element;
//        length++;
//    }
//
//    @Override
//    public void insertTail(T element) {
//        if (length == store.length) {
//            var dest = new Object[length + increaseStep];
//            System.arraycopy(store, 0, dest, 0, length);
//            store = dest;
//        }
//        store[length] = element;
//        length++;
//    }

    @Override
    public void delete(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        if (length - 1 <= store.length - increaseStep) {
            var dest = new Object[length - increaseStep];
            System.arraycopy(store, 0, dest, 0, index);
            System.arraycopy(store, index + 1, dest, index, length - index - 1);
            store = dest;
        } else {
            for (int i = index; i < length - 1; i++) {
                store[i] = store[i + 1];
            }
        }
        length--;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return (T) store[index];
    }

//    @Override
//    @SuppressWarnings("unchecked")
//    public T getHead() {
//        if (length == 0) {
//            throw new IndexOutOfBoundsException();
//        }
//        return (T) store[0];
//    }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public T getTail() {
//        if (length == 0) {
//            throw new IndexOutOfBoundsException();
//        }
//        return (T) store[length - 1];
//    }

    @Override
    public void set(T element, int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        store[index] = element;
    }

    @Override
    public void clear() {
        store = new Object[initialCapacity];
        length = 0;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Iterator iterator() {
        return new Iterator<T>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                return (T) store[index++];
            }

            @Override
            public void reset() {
                index = 0;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        return (T[]) Arrays.copyOf(store, length);
    }
}
