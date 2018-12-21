package Lab3

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * 采用顺序表实现
 */
class HuffmanCodingTree {

    private lateinit var store: Array<Node>
    private var rootIndex: Int = 0

    /**
     * Huffman Tree的每个节点
     */
    data class Node(val data: Int = -1, var weight: Long = -1, var isLeaf: Boolean = false) {
        var parentIndex: Int = -1
        var childIndex = intArrayOf(-1, -1)  //0是左，1是右
    }

    /**
     * 只需要储存0~255位置的权重，即可恢复出huffman树
     */
    @Throws(IOException::class)
    fun saveToFile(output: DataOutputStream) {
        for (i in 0..255) {
            output.writeLong(store[i].weight)
        }
    }

    /**
     * 从文件中恢复
     */
    @Throws(IOException::class)
    fun restoreFromFile(input: DataInputStream) {
        store = Array(256 * 2 - 1) { i ->
            if (i in 0..255) {
                Node(i, input.readLong(), true)
            } else {
                Node()
            }
        }
        completeHuffmanTree()
    }

    constructor()

    /**
     * 将Huffman树补全
     */
    private fun completeHuffmanTree() {
        //每次找到0~i之间最小的两个节点，组合并加入数组
        for (i in 256 until store.size) {
            //要寻找的最小权重的位置
            var minIndex1 = -1
            var minIndex2 = -1
            var minWeight1 = java.lang.Long.MAX_VALUE
            var minWeight2 = java.lang.Long.MAX_VALUE
            for (j in 0 until i) {
                if (store[j].parentIndex == -1) {
                    if (store[j].weight < minWeight1) {
                        minIndex2 = minIndex1
                        minIndex1 = j
                        minWeight2 = minWeight1
                        minWeight1 = store[j].weight
                    } else if (store[j].weight < minWeight2) {
                        minIndex2 = j
                        minWeight2 = store[j].weight
                    }
                }
            }
            //创建新新节点，孩子节点是找到的权重最小的两个节点，新节点权重是两个孩子节点权重之和
            store[i].weight = store[minIndex1].weight + store[minIndex2].weight
            store[minIndex1].parentIndex = i
            store[minIndex2].parentIndex = i
            store[i].childIndex[0] = minIndex1
            store[i].childIndex[1] = minIndex2
        }
        //记录root的位置
        rootIndex = store.size - 1
    }

    /**
     * 读取文件流构造Huffman树
     */
    @Throws(IOException::class)
    constructor(input: DataInputStream) {
        store = Array(256 * 2 - 1) { i ->
            if (i in 0..255) {
                Node(i, 0, true)
            } else {
                Node()
            }
        }
        var b: Int
        while (true) {
            b = input.read()
            if (b == -1) {
                break
            }
            store[b].weight++
        }
        completeHuffmanTree()
    }

    /**
     * 编码byte流
     *
     * @param input  输入的byte流
     * @param output 输出的byte流
     * @return 输出bit流的bit数
     */
    @Throws(IOException::class)
    fun encode(input: DataInputStream, output: DataOutputStream): Long {
        val out = BitOutputStream(output)
        var length: Long = 0  //编码后bit长度
        val tmp = IntArray(256)
        var now: Int
        while (true) {
            now = input.read()
            if (now == -1) {
                break
            }
            //叶结点逐步回到根节点
            var parent = store[now].parentIndex
            var tmpLen = 0
            while (parent != -1) {
                tmp[tmpLen++] = if (store[parent].childIndex[0] == now) 0 else 1
                now = parent
                parent = store[now].parentIndex
            }
            for (i in tmpLen - 1 downTo 0) {
                out.write(tmp[i])
            }
            length += tmpLen.toLong()
        }
        out.flush()
        return length
    }

    /**
     * 解码bit流
     *
     * @param input  输入的byte流
     * @param limit  读取的bit数限制，避免编码时最后一个byte补的0被算作有效的编码
     * @param output 输出的byte流
     */
    @Throws(IOException::class)
    fun decode(input: DataInputStream, limit: Long, output: DataOutputStream) {
        val bitIn = BitInputStream(input)
        var rest = limit
        while (rest > 0) {
            var now = rootIndex
            while (!store[now].isLeaf) {
                val b = bitIn.read()
                if (b == -1) {
                    throw IOException()
                }
                rest--
                now = store[now].childIndex[b]
            }
            output.writeByte(store[now].data)
        }
    }
}

/**
 * bit输入流
 */
internal class BitInputStream(private val input: DataInputStream) {
    private var temp: Int = 0
    private var rest: Int = 0

    @Throws(IOException::class)
    fun read(): Int {
        if (rest == 0) {
            temp = input.read()
            if (temp == -1) {
                return -1
            }
            rest = 8
        }
        rest--
        return (temp shr rest) and 1
    }
}

/**
 * bit输出流
 */
internal class BitOutputStream(private val output: DataOutputStream) {
    private var temp: Int = 0
    private var tempLen: Int = 0

    @Throws(IOException::class)
    fun write(bit: Int) {
        temp = temp shl 1
        temp += bit
        tempLen++
        if (tempLen == 8) {
            output.writeByte(temp)
            tempLen = 0
        }
    }

    @Throws(IOException::class)
    fun flush() {
        temp = temp shl (8 - tempLen)
        output.write(temp)
    }
}