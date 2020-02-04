package utils

class PriorityQueue<E : Any>(val comparator: Comparator<E>) {

    private var queue: ArrayList<E> = ArrayList(11)

    val size get() = queue.size

    fun isNotEmpty(): Boolean = size > 0

    fun offer(e: E) {
        val index = queue.binarySearch(e, comparator).let {
            if (it < 0) -it -1 else it
        }
        queue.add(index, e)
    }

    fun poll():E {
        check(size > 0)
        return queue.removeAt(0)
    }

}
