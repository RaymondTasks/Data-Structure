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

    public Node getNewNode(T data, Node next) {
        return new Node(data, next);
    }

    protected Node head;  //头结点
    protected Node tail;  //尾节点

    public LinkedList() {
        head = new Node(null, null);
        tail = head;
    }

    public LinkedList(T[] data) {  //用数组创建链表
        if (data.length != 0) {
            tail = new Node(data[data.length - 1], null);
            Node p = tail;
            for (int i = data.length - 2; i >= 0; i--) {
                p = new Node(data[i], p);
            }
            head = new Node(null, p);
        } else {
            head = new Node(null, null);
            tail = head;
        }
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

    public T getHead() {
        if (head.getNext() != null) {
            return head.getNext().getData();
        }
        throw new IndexOutOfBoundsException();
    }

    public T getTail() {
        if (tail != head) {
            return tail.getData();
        }
        throw new IndexOutOfBoundsException();
    }

    public Node getHeadNode() {
        return head;
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
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        var arr = new Object[getLength()];
        Node p = head.getNext();
        int i = 0;
        while (p != null) {
            arr[i] = p.getData();
            p = p.getNext();
            i++;
        }
        return (T[]) arr;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
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
