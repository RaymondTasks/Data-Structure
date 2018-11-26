package Tree;

import Queue.LinkedQueue;

import java.io.Serializable;

/**
 * 采用二叉链表实现
 */
public class BinaryTree<T> implements Serializable {
    public class Node {
        private T data;
        private Node leftChild;
        private Node rightChild;

        public Node(T data, Node leftChild, Node rightChild) {
            this.data = data;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
        }

        public T getData() {
            return data;
        }

        public Node getLeftChild() {
            return leftChild;
        }

        public Node getRightChild() {
            return rightChild;
        }

        public void setData(T data) {
            this.data = data;
        }

        public void setLeftChild(Node leftChild) {
            this.leftChild = leftChild;
        }

        public void setRightChild(Node rightChild) {
            this.rightChild = rightChild;
        }
    }

    private Node root;

    public BinaryTree() {
        root = null;
    }

    public Node getRoot() {
        return root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int getDegree() {
        var queue = new LinkedQueue<Node>();
        if (root != null) {
            queue.add(root);
        }
        int degree = 0;
        while (!queue.isEmpty()) {
            var node = queue.get();
            degree++;
            var lchild = node.getLeftChild();
            if (lchild != null) {
                queue.add(lchild);
            }
            var rchild = node.getRightChild();
            if (rchild != null) {
                queue.add(rchild);
            }
        }
        return degree;
    }

    public Node getParent(Node node) {
        if (node == root || node == null) {
            return null;
        }
        var queue = new LinkedQueue<Node>();
        if (root != null) {
            queue.add(root);
        }
        while (!queue.isEmpty()) {
            var tmp = queue.get();
            var lchild = node.getLeftChild();
            var rchild = node.getRightChild();
            if (lchild == node || rchild == node) {
                return tmp;
            }
            if (lchild != null) {
                queue.add(lchild);
            }
            if (rchild != null) {
                queue.add(rchild);
            }
        }
        return null;
    }
}
