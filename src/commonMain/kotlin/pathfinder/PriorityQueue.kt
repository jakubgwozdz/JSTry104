package pathfinder

// Based on OpenJDK implementation

class PriorityQueue<E : Any>(val comparator: Comparator<E>) {

    private var queue: Array<Any?> = arrayOfNulls(11)

    var size: Int = 0
        private set

    fun isNotEmpty(): Boolean = size > 0

    fun poll(): E {
        check(size > 0)
        val es: Array<Any?> = queue
        val result = queue[0]!! as E
        val n: Int = --size
        val x = es[n] as E
        es[n] = null
        if (n > 0) {
            siftDown(0, x, n)
        }
        return result
    }

    fun offer(e: E) {
        val i = size
        if (i >= queue.size) grow(i + 1)
        siftUp(i, e)
        size = i + 1
    }

    private fun grow(minCapacity: Int) {
        val oldCapacity = queue.size
        // Double size if small; else grow by 50%
        val newCapacity =
            oldCapacity + if (oldCapacity < 64) oldCapacity + 2 else oldCapacity shr 1
        queue = queue.copyOf(newCapacity)
    }

    private fun siftUp(k: Int, x: E) {
        var k1 = k
        while (k1 > 0) {
            val parent = k1 - 1 ushr 1
            val e = queue[parent]!! as E
            if (comparator.compare(x, e) >= 0) break
            queue[k1] = e
            k1 = parent
        }
        queue[k1] = x
    }

    private fun siftDown(k: Int, x: E, n: Int) {
        var k1 = k
        val half = n ushr 1
        while (k1 < half) {
            var child = (k1 shl 1) + 1
            var c = queue[child]!! as E
            val right = child + 1
            if (right < n && comparator.compare(c, queue[right]!! as E) > 0) {
                child = right
                c = queue[child]!! as E
            }
            if (comparator.compare(x, c) <= 0) break
            queue[k1] = c
            k1 = child
        }
        queue[k1] = x
    }


}
