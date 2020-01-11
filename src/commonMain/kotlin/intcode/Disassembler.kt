package intcode

sealed class Param {
    abstract val value: Long
}

data class PositionParam(override val value: Long) : Param() {
    override fun toString(): String {
        return "[$value]"
    }
}

data class ImmediateParam(override val value: Long) : Param() {
    override fun toString(): String {
        return "$value"
    }
}

data class RelativeParam(override val value: Long) : Param() {
    override fun toString(): String {
        return when {
            value < 0L -> "[rb$value]"
            value == 0L -> "[rb]"
            value > 0L -> "[rb+$value]"
            else -> error("$value")
        }
    }
}

sealed class Disassembly {
    abstract val size: Long
}

data class Op(val name: String, val params: List<Param>) : Disassembly() {
    override fun toString(): String {
        return "$name ${params.joinToString(", ")}"
    }

    override val size: Long
        get() = 1L + params.size
}

data class Data(val value: Long) : Disassembly() {
    override fun toString(): String {
        return "DATA  $value"
    }

    override val size: Long
        get() = 1
}

fun dissassembly(memory: Memory, addr: Long): Disassembly {

    fun params(count: Int): List<Param> {
        return (1..count).map {
            when (nthParamMode(it, memory[addr])) {
                ParamMode.Position -> PositionParam(memory[addr + it])
                ParamMode.Immediate -> ImmediateParam(memory[addr + it])
                ParamMode.Relative -> RelativeParam(memory[addr + it])
            }
        }
    }

    return try {
        val opcode = opcode(memory[addr])
        when (opcode) {
            1L -> Op("ADD  ", params(3))
            2L -> Op("MUL  ", params(3))
            3L -> Op("IN   ", params(1))
            4L -> Op("OUT  ", params(1))
            5L -> Op("JNZ  ", params(2))
            6L -> Op("JZ   ", params(2))
            7L -> Op("SETL ", params(3))
            8L -> Op("SETE ", params(3))
            9L -> Op("MOV   rb,", params(1))
            99L -> Op("HALT ", emptyList())
            else -> error("unknown opcode ${memory[addr]} at addr $addr")
        }
    } catch (e: Exception) {
        Data(memory[addr])
    }
}

fun disassemblyProgram(program: String): Sequence<String> {
    val memory = parseIntcode(program)
    var addr = 0L

    return object : Iterator<String> {
        override fun hasNext(): Boolean = addr < memory.size
        override fun next(): String = dissassembly(memory, addr)
            .let { "${addr.toString().padStart(6)}: $it" to it.size }
            .also { addr += it.second }
            .first
    }
        .asSequence()
}
