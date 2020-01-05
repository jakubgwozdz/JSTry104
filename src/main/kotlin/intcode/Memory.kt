package intcode

class Memory(initial: Map<Long, Long>) {
    val map = initial.toMutableMap()
    operator fun get(addr: Long) = map[addr] ?: 0L
    operator fun set(addr: Long, value: Long) {
        map[addr] = value
    }

    val size: Long get() = map.keys.max() ?: 0L

    fun copy() = Memory(map.toMutableMap())
}

fun opcode(operation: Long) = operation % 100

// params handling
enum class ParamMode { Position, Immediate, Relative }

fun nthParamMode(n: Int, operation: Long): ParamMode {
    var mode = operation / 10
    repeat(n) { mode /= 10 }
    return when (mode % 10) {
        0L -> ParamMode.Position
        1L -> ParamMode.Immediate
        2L -> ParamMode.Relative
        else -> error("Unknown mode in opcode $operation")
    }
}

fun parseIntcode(input: String): Memory {
    return input
        .split(",")
        .mapIndexed { index: Int, s: String -> index.toLong() to s.trim().toLong() }
        .toMap()
        .let { Memory(it) }
}

