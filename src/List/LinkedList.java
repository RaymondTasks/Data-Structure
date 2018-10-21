package List;

public class LinkedList<T> implements List<T> {

    public class Node {
        private T data;
        private Node next;

        public Node(T data, Node next) {
            this.data = data;
            this.next = next;
        }

        public T getData() {
            return data;
        }

        public Node getNext() {
            return next;
        }

        public void setData(T data) {
            this.data = data;
        }

        public void setNext(Node next) {
            this.next = next;
        }
    }

    private Node head;  //头结点
    private Node tail;  //尾节点

    public LinkedList() {
        head = new Node(null, null);
        tail = head;
    }

    public LinkedList(T[] data) {  //用数组创建链表
        Node p = null;
        for (int i = data.length - 1; i >= 0; i--) {
            p = new Node(data[i], p);
        }
        head = new Node(null, p);
    }

    @Override
    public void insert(T element, int index) {
        Node p = head;
        int i = -1;
        while (p != null) {
            i++;
            if (i == index) {
                Node n = new Node(element, p.getNext());  //要插入的位置是 p.next
                p.setNext(n);
                if (p == tail) {  //插入前是尾节点
                    tail = n;
                }
                return;
            }
            p = p.getNext();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void insertTail(T element) {
        Node n = new Node(element, null);
        tail.setNext(n);
        tail = n;
    }

    @Override
    public void delete(int index) {
        Node p = head;
        int i = -1;
        while (p.getNext() != null) {  //要删除的是 p.next
            i++;
            if (i == index) {
                p.setNext(p.getNext().getNext());
                if (p.getNext() == null) {
                    tail = p;
                }
                return;
            }
            p = p.getNext();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public T get(int index) {
        Node p = head.getNext();
        int i = 0;
        while (p != null) {
            if (i == index) {
                return p.getData();
            }
            p = p.getNext();
            i++;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void set(T element, int index) {
        Node p = head.getNext();
        int i = 0;
        while (p != null) {
            if (i == index) {
                p.setData(element);
                return;
            }
            p = p.getNext();
            i++;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void clear() {
        head.setNext(null);
        tail = head;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return head == tail;
    }

    @Override
    public int getLength() {
        Node p = head.getNext();
        int len = 0;
        while (p != null) {
            len++;
            p = p.getNext();
        }
        return len;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object[] toArray() {
        Object[] obj = new Object[getLength()];
        Node p = head.getNext();
        int i = 0;
        while (p != null) {
            obj[i] = p.getData();
            p = p.getNext();
            i++;
        }
        return obj;
    }

    @Override
    public Iterator iterator() {
        return new List.List.Iterator<T>() {
            private Node now = head;

            @Override
            public boolean hasNext() {
                return now.getNext() != null;
            }

            @Override
            public T next() {
                now = now.getNext();
                return now.getData();
            }

            @Override
            public void reset() {
                now = head;
            }
        };
    }
}
