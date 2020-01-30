package intcode

import kotlin.test.Test
import kotlin.test.expect

class MemoryTest {

    @Test
    fun shouldParseInputString() {
        expect(1002L) { parseIntcode("1002,4,3,4,33")[0] }
    }
}