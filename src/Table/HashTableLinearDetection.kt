package Table

import kotlin.math.abs

/**
 * 使用线性探测法解决冲突的 Hash Table
 */
class HashTableLinearDetection<K, E>(val capacity: Int = 100) : HashTable<K, E> {

    private data class Node<K, E>(var key: K, var element: E?)

    private val store: Array<Node<K, E>?> = arrayOfNulls(capacity)

    override fun put(key: K, element: E?) {
        val addr = hash(key)
        for (i in 0 until capacity) {
            val index = (addr + i) % capacity
            if (store[index] == null) {
                //找到空位
                store[index] = Node(key, element)
                return
            } else if (store[index]!!.key == key) {
                //存在重复的key，覆盖
                store[index]!!.element = element
                return
            }
        }
        //表已满
        throw  FullTableException()
    }

    override fun get(key: K): E? {
        val addr = hash(key)
        for (i in 0 until capacity) {
            val index = (addr + i) % capacity
            if (store[index] != null && store[index]!!.key == key) {
                SL = i + 1
                return store[index]!!.element
            }
        }
        //未找到key
        SL = capacity
        throw KeyNotFoundException()
    }

    override fun delete(key: K) {
        val addr = hash(key)
        for (i in 0 until capacity) {
            val index = (addr + i) % capacity
            if (store[index] != null && store[index]!!.key == key) {
                store[index] = null
                return
            }
        }
        //未找到key
        throw  KeyNotFoundException()
    }

    override fun hash(key: K): Int =
            when (key) {
                is Number -> abs(key.toInt() % capacity)
                else -> abs(key.hashCode() % capacity)
            }

    override fun getSize(): Int {
        var len = 0
        store.forEach {
            if (it != null) {
                len++
            }
        }
        return len
    }

    override var SL = 0

}
