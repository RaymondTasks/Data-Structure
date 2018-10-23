package List;

import java.util.Iterator;

public interface List<T> {

    public void insert(T element, int index);  //插入到index

    public void insertTail(T element);  //插入到末尾

    public void delete(int index);  //删除index

    public T get(int index);  //获得index地元素

    public void set(T element, int index);  //设置Index

    public void clear();

    public boolean isFull();

    public boolean isEmpty();

    public int getLength();

    public int getCapacity();

    public Iterator iterator();

    public T[] toArray();

    public interface Iterator<T> {
        public boolean hasNext();

        public T next();

        public void reset();
    }


}
