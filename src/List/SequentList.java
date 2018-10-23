package List;

import java.util.Arrays;

public class SequentList<T> implements List<T> {

    private Object[] data;

    private int length, initialCapacity, increaseStep;

    public SequentList(int initialCapacity, int increaseStep) {
        data = new Object[initialCapacity];
        length = 0;
        this.initialCapacity = initialCapacity;
        this.increaseStep = increaseStep;
    }

    private void checkAndAdjustCapacity() {
        if (length > data.length || length <= data.length - increaseStep) {
            int newCapacity;
            if (length > data.length) {
                newCapacity = data.length + increaseStep;
            } else {
                newCapacity = data.length - increaseStep;
            }
            var n = new Object[newCapacity];
            System.arraycopy(data, 0, n, 0, length);
            data = n;
        }
    }

    @Override
    public void insert(T element, int index) {
        if (index < 0 || index > length) {
            throw new IndexOutOfBoundsException();
        }
        length++;
        checkAndAdjustCapacity();
        for (int i = length - 2; i >= index; i--) {
            data[i + 1] = data[i];
        }
        data[index] = element;
    }

    @Override
    public void insertTail(T element) {
        length++;
        checkAndAdjustCapacity();
        data[length - 1] = element;
    }

    @Override
    public void delete(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = index; i < length; i++) {
            data[i] = data[i + 1];
        }
        length--;
        checkAndAdjustCapacity();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return (T) data[index];
    }

    @Override
    public void set(T element, int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        data[index] = element;
    }

    @Override
    public void clear() {
        data = new Object[initialCapacity];
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
                return (T) data[index++];
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
        return (T[]) Arrays.copyOf(data, length);
    }
}
