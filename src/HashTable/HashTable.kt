package HashTable

interface HashTable<K, E> {
    /**
     * 添加元素
     */
    fun put(key: K, element: E?)

    /**
     * 获取元素
     */
    fun get(key: K): E?

    /**
     * 删除元素
     */
    fun delete(key: K)

    /**
     * hash函数
     */
    fun hash(key: K): Int

    /**
     * 获取当前元素个数
     */
    fun getSize(): Int

    /**
     * 每次get后更新的平均查找长度
     */
    var SL: Int

}