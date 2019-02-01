package Queue

class OrderedLinkedQueue<T>(
        private val comparator: Comparator<T>
) : LinkedQueue<T>() {

    override fun add(element: T) {
        var p = store.headNode
        while (p.next != null) {
            if (comparator.compare(element, p.next.data) > 0) {
                p = p.next
            } else {
                p.next = store.getNewNode(element, p.next)
                return
            }
        }
        store.insertTail(element)
    }
}
