package HashTable

import kotlin.math.abs


/**
 * 使用链表法解决冲突的 Hash Table
 */
class HashTableLinkedList<K, E>(private val locations: Int = 100) : HashTable<K, E> {

    private data class Node<K, E>(var key: K,
                                  var element: E?,
                                  var next: Node<K, E>? = null)

    //储存元素
    private val store: Array<Node<K, E>?> = arrayOfNulls(locations)

    override fun put(key: K, element: E?) {
        val addr = hash(key)
        var p = store[addr]
        var q: Node<K, E>? = null   //q是p前一个节点
        while (p != null) {
            if (p.key == key) {
                //key已存在则替换
                p.element = element
                return
            }
            q = p
            p = p.next
        }
        //没有找到key,添加一个节点
        if (q != null) {
            q.next = Node(key, element)
        } else {
            store[addr] = Node(key, element)
        }

    }

    /**
     * 根据key获取元素
     * key不存在则抛出exception
     */
    override fun get(key: K): E? {
        SL = 0
        val addr = hash(key)
        var p = store[addr]
        while (true) {
            SL++
            if (p == null) {
                break
            }
            if (p.key == key) {
                return p.element
            }
            p = p.next
        }
        //未找到key
        throw KeyNotFoundException()
    }

    override fun delete(key: K) {
        val addr = hash(key)
        var p = store[addr]
        var q: Node<K, E>? = null   //q是p前一个节点
        while (p != null) {
            if (p.key == key) {
                if (q == null) {
                    store[addr] = p.next
                } else {
                    q.next = p.next
                }
                return
            }
            q = p
            p = p.next
        }
        //没有找到key
        throw KeyNotFoundException()
    }

    override fun hash(key: K): Int =
            when (key) {
                is Number -> abs(key.toInt() % locations)
                else -> abs(key.hashCode() % locations)
            }


    override fun getSize(): Int {
        var size = 0
        for (item in store) {
            var p = item
            while (p != null) {
                size++
                p = p.next
            }
        }
        return size
    }

    override var SL = 0
}

