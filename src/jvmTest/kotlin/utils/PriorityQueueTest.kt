package utils

import org.junit.jupiter.api.Test
import kotlin.test.expect

internal class PriorityQueueTest {

    @Test
    fun operations() {

        val queue = PriorityQueue<Int>(compareBy { it })

        queue.offer(3)
        queue.offer(5)
        queue.offer(13)
        queue.offer(2)
        queue.offer(11)
        expect(2) { queue.poll() }
        expect(3) { queue.poll() }
        expect(5) { queue.poll() }
        expect(11) { queue.poll() }
        expect(13) { queue.poll() }

        queue.offer(12)
        queue.offer(2)
        expect(2) { queue.poll() }
        queue.offer(32)
        queue.offer(11)
        expect(11) { queue.poll() }
        expect(12) { queue.poll() }
        expect(true) { queue.isNotEmpty() }
        expect(32) { queue.poll() }
        expect(false) { queue.isNotEmpty() }
    }
}
