package Lab3;

import java.io.*;

/**
 * 采用顺序表实现
 */
public class HuffmanCodingTree implements Serializable {
    public class Node implements Serializable {
        private byte data;
        private long weight;
        private int parentIndex;
        private int leftChildIndex;
        private int rightChildIndex;

        public Node(byte data, long weight) {
            this.data = data;
            this.weight = weight;
            this.leftChildIndex = -1;
            this.rightChildIndex = -1;
            parentIndex = -1;
        }

        public boolean isLeaf() {
            return leftChildIndex == -1 && rightChildIndex == -1;
        }
    }

    private Node[] store;
    private int rootIndex;

    /**
     * 只需要储存0~255位置的权重，即可恢复出huffman树
     */
    public void saveToFile(DataOutputStream output) throws IOException {
        for (int i = 0; i < 256; i++) {
            output.writeLong(store[i].weight);
        }
    }

    public void restoreFromFile(DataInputStream input) throws IOException {
        store = new Node[256 * 2 - 1];
        for (int i = 0; i < 256; i++) {
            store[i] = new Node((byte) i, input.readLong());
        }
        completeHuffmanTree();
    }

    public HuffmanCodingTree() {
    }

    /**
     * 将Huffman树补全
     */
    private void completeHuffmanTree() {
        //每次找到0~i之间最小的两个节点，组合并加入数组
        for (int i = 256; i < store.length; i++) {
            int minIndex1 = -1, minIndex2 = -1;
            long minWeight1 = Long.MAX_VALUE, minWeight2 = Long.MAX_VALUE;

            for (int j = 0; j < i; j++) {
                if (store[j].parentIndex == -1) {
                    if (store[j].weight < minWeight1) {
                        minIndex2 = minIndex1;
                        minIndex1 = j;
                        minWeight2 = minWeight1;
                        minWeight1 = store[j].weight;
                    } else if (store[j].weight < minWeight2) {
                        minIndex2 = j;
                        minWeight2 = store[j].weight;
                    }
                }
            }
            store[i] = new Node((byte) 0, store[minIndex1].weight + store[minIndex2].weight);
            store[minIndex1].parentIndex = i;
            store[minIndex2].parentIndex = i;
            store[i].leftChildIndex = minIndex1;
            store[i].rightChildIndex = minIndex2;
        }
        rootIndex = store.length - 1;
    }

    public HuffmanCodingTree(BufferedInputStream input) throws IOException {
        store = new Node[256 * 2 - 1];
        for (int i = 0; i < 256; i++) {
            store[i] = new Node((byte) i, 0);
        }
        int b;
        while ((b = input.read()) != -1) {
            store[b].weight++;
        }
        completeHuffmanTree();
    }

    /**
     * 编码byte流
     */
    public long encode(BufferedInputStream input, BufferedOutputStream output) throws IOException {
        var out = new BitOutputStream(output);
        long length = 0;  //编码后bit长度
        byte tmp[] = new byte[256];
        int b;
        while ((b = input.read()) != -1) {
            //叶结点逐步回到根节点
            int now = b;
            int parent = store[now].parentIndex;
            int tmp_len = 0;
            while (parent != -1) {
                //性能瓶颈处
                tmp[tmp_len++] = store[parent].leftChildIndex == now ? (byte) 0 : (byte) 1;
                now = parent;
                parent = store[now].parentIndex;
            }
            for (int i = tmp_len - 1; i >= 0; i--) {
                out.write(tmp[i]);
            }
            length += tmp_len;
        }
        out.flush();
        return length;
    }

    /**
     * 解码bit流
     */
    public void decode(BufferedInputStream input, long limit, BufferedOutputStream output) throws IOException {
        BitInputStream in = new BitInputStream(input, limit);
        for (; ; ) {
            int now = rootIndex;
            for (; ; ) {
                int b = in.read();
                if (b == 0) {
                    now = store[now].leftChildIndex;
                } else if (b == 1) {
                    now = store[now].rightChildIndex;
                } else {
                    return;
                }
                if (store[now].isLeaf()) {
                    break;
                }
            }
            output.write(store[now].data);
        }
    }
}

/**
 * bit输入流
 */
class BitInputStream {
    private BufferedInputStream input;
    private long limit;
    private int b;
    private int rest;

    public BitInputStream(BufferedInputStream input, long limit) {
        this.input = input;
        this.limit = limit;
        rest = 0;
    }

    public int read() throws IOException {
        if (limit == 0) {
            return -1;
        }
        if (rest == 0) {
            b = input.read();
            if (b == -1) {
                throw new IOException();
            } else {
                rest = 8;
            }
        }
        rest--;
        limit--;
        return (byte) ((b >> rest) & 1);
    }
}

/**
 * bit输出流
 */
class BitOutputStream {
    private BufferedOutputStream output;

    public BitOutputStream(BufferedOutputStream output) {
        this.output = output;
        rest = new byte[8];
        restLength = 0;
    }

    private byte[] rest;
    private int restLength;

    public void write(byte bit) throws IOException {
        rest[restLength++] = bit;
        if (restLength == 8) {
            flush();
        }
    }

    public void flush() throws IOException {
        byte tmp = 0;
        for (int i = 0; i < restLength; i++) {
            tmp <<= 1;
            tmp += rest[i];
        }
        output.write(tmp << (8 - restLength));
        restLength = 0;
    }
}