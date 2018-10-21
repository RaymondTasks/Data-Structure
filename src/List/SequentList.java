package List;

public class SequentList<T> implements List<T> {

    private Object[] data;

    private int length, increaseStep;

    public SequentList(int initialCapacity, int increaseStep) {
        data = new Object[initialCapacity];
        length = 0;
        this.increaseStep = increaseStep;
    }

    private SequentList(T[] data){

    }

    @Override
    public void insert(T element, int index) {

    }

    @Override
    public void insertTail(T element) {

    }

    @Override
    public void delete(int index) {

    }

    @Override
    public T get(int index) {
        return null;
    }

    @Override
    public void set(T element, int index) {

    }

    @Override
    public void clear() {

    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }
}
