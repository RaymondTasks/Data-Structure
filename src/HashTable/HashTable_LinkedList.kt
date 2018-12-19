package HashTable


class HashTable_LinkedList<K, E>(val locations: Int = 100) : HashTable<K, E> {

    private data class Node<K, E>(var key: K, var element: E?, var next: Node<K, E>?)

    //储存元素
    private var store: Array<Node<K, E>?> = arrayOfNulls(locations)

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
            q.next = Node(key, element, null)
        } else {
            store[addr] = Node(key, element, null)
        }

    }

    override fun get(key: K): E? {
        val addr = hash(key)
        var p = store[addr]
        while (p != null) {
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
                is Number -> key.toInt() % locations
                else -> key.hashCode() % locations
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
}

class KeyNotFoundException : Exception()