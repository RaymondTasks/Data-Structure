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
        private int childIndex[] = new int[2];  //0是左，1是右
        private boolean isLeaf;

        public Node(byte data, long weight) {
            this.data = data;
            this.weight = weight;
            childIndex[0] = -1;
            childIndex[1] = -1;
            parentIndex = -1;
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
            store[i].isLeaf = true;
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
            //要寻找的最小权重的位置
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
            //创建新新节点，孩子节点是找到的权重最小的两个节点，新节点权重是两个孩子节点权重之和
            store[i] = new Node((byte) 0, store[minIndex1].weight + store[minIndex2].weight);
            store[i].isLeaf = false;
            store[minIndex1].parentIndex = i;
            store[minIndex2].parentIndex = i;
            store[i].childIndex[0] = minIndex1;
            store[i].childIndex[1] = minIndex2;
        }
        //记录root的位置
        rootIndex = store.length - 1;
    }

    public HuffmanCodingTree(BufferedInputStream input) throws IOException {
        store = new Node[256 * 2 - 1];
        for (int i = 0; i < 256; i++) {
            store[i] = new Node((byte) i, 0);
            store[i].isLeaf = true;
        }
        int b;
        while ((b = input.read()) != -1) {
            store[b].weight++;
        }
        completeHuffmanTree();
    }

    /**
     * 编码byte流
     *
     * @param input  输入的byte流
     * @param output 输出的byte流
     * @return 输出bit流的bit数
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
                tmp[tmp_len++] = store[parent].childIndex[0] == now ? (byte) 0 : (byte) 1;
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
     *
     * @param input  输入的byte流
     * @param limit  读取的bit数限制，避免编码时最后一个byte补的0被算作有效的编码
     * @param output 输出的byte流
     */
    public void decode(BufferedInputStream input, long limit, BufferedOutputStream output) throws IOException {
        BitInputStream in = new BitInputStream(input);
        long rest = limit;
        while (rest > 0) {
            int now = rootIndex;
            while (!store[now].isLeaf) {
                int b = in.read();
                if (b == -1) {
                    throw new IOException();
                }
                rest--;
                now = store[now].childIndex[b];
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
    private int temp;
    private int rest;

    public BitInputStream(BufferedInputStream input) {
        this.input = input;
        rest = 0;
    }

    public int read() throws IOException {
        if (rest == 0) {
            temp = input.read();
            if (temp == -1) {
                return -1;
            }
            rest = 8;
        }
        rest--;
        return (temp >> rest) & 1;
    }
}

/**
 * bit输出流
 */
class BitOutputStream {
    private BufferedOutputStream output;
    private byte temp;
    private int temp_len;

    public BitOutputStream(BufferedOutputStream output) {
        this.output = output;
        temp_len = 0;
    }

    public void write(byte bit) throws IOException {
        temp <<= 1;
        temp += bit;
        temp_len++;
        if (temp_len == 8) {
            output.write(temp);
            temp_len = 0;
        }
    }

    public void flush() throws IOException {
        temp <<= 8 - temp_len;
        output.write(temp);
    }
}