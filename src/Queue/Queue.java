package Queue;

public interface Queue<T> {
    public int getSize();

    public int getCapacity();

    public boolean isEmpty();

    public boolean isFull();

    public void add(T element);

    public T get() ;

    public T getHead();

    public T getTail() ;

    public void clear();

    public interface Iterator<T> {
        public boolean hasNext();

        public T next();

        public void reset();
    }

    public Iterator<T> iterator();
}
