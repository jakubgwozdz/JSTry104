package intcode

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

interface InBuffer<T> {
    suspend fun receive(): T
}

interface OutBuffer {
    suspend fun send(v: Long)
    fun close(): Boolean
}

class ChannelInBuffer<T>(val id: Any, val channel: ReceiveChannel<T>, val logIO: Boolean = false) :
    InBuffer<T> {
    override suspend fun receive(): T {
        if (logIO) print("$id <-- ...")
        return channel.receive().also { if (logIO) println("\b\b\b$it") }
    }
}

class ChannelOutBuffer(val id: Any, val channel: SendChannel<Long>, val logIO: Boolean = false) : OutBuffer {
    override suspend fun send(v: Long) {
        channel.also { if (logIO) print("$id --> $v...") }.send(v).also { if (logIO) println("\b\b\b done") }
    }

    override fun close() = channel.close()
}

class Intcode(
    val memory: Memory,
    val inBuffer: InBuffer<Long>,
    val outBuffer: OutBuffer,
    val id: Any = "Intcode",
    val debug: (Intcode) -> Unit = {}
) {
    constructor(
        memory: Memory,
        receiveChannel: ReceiveChannel<Long>,
        sendChannel: SendChannel<Long>,
        id: Any = "Intcode",
        debug: (Intcode) -> Unit = {}
    ) : this(memory, ChannelInBuffer(id, receiveChannel), ChannelOutBuffer(id, sendChannel), id, debug)

    var ip: Long = 0 // instruction pointer
    var rb: Long = 0 // relative base

    suspend fun read(): Long {
        val job = GlobalScope.launch {
            delay(1000)
            println("Computer $id waits for input> ")
        }
        return try {
            inBuffer.receive()
        } finally {
            job.cancel()
        }
    }

    suspend fun write(v: Long) {
        val job = GlobalScope.launch {
            delay(1000)
            println("Computer $id wants to write> ")
        }
        try {
            outBuffer.send(v)
        } finally {
            job.cancel()
        }
    }

    val operation: Long get() = memory[ip]

    private fun nthAddr(n: Int): Long =
        when (nthParamMode(n, operation)) {
            ParamMode.Position -> memory[ip + n]
            ParamMode.Immediate -> ip + n
            ParamMode.Relative -> rb + memory[ip + n]
        }

    private val firstAddr get() = nthAddr(1)
    private val secondAddr get() = nthAddr(2)
    private val thirdAddr get() = nthAddr(3)

    // main loop
    suspend fun run() {
        while (true) {
            debug(this)
            when (opcode(operation)) {
                1L -> opADD()
                2L -> opMUL()
                3L -> opIN()
                4L -> opOUT()
                5L -> opJNZ()
                6L -> opJZ()
                7L -> opSETL()
                8L -> opSETE()
                9L -> opMOVRB()
                99L -> {
//                    outBuffer.close()
                    return
                }
                else -> error("unknown opcode $operation at addr $ip")
            }
            yield()
        }
    }

    // operations

    private fun opADD() {
        memory[thirdAddr] = memory[firstAddr] + memory[secondAddr]
        ip += 4
    }

    private fun opMUL() {
        memory[thirdAddr] = memory[firstAddr] * memory[secondAddr]
        ip += 4
    }

    private suspend fun opIN() {
        memory[firstAddr] = read()
        ip += 2
    }

    private suspend fun opOUT() {
        write(memory[firstAddr])
        ip += 2
    }

    private fun opJNZ() {
        ip = when {
            memory[firstAddr] != 0L -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opJZ() {
        ip = when {
            memory[firstAddr] == 0L -> memory[secondAddr]
            else -> ip + 3
        }
    }

    private fun opSETL() {
        memory[thirdAddr] = if (memory[firstAddr] < memory[secondAddr]) 1L else 0L
        ip += 4
    }

    private fun opSETE() {
        memory[thirdAddr] = if (memory[firstAddr] == memory[secondAddr]) 1L else 0L
        ip += 4
    }

    private fun opMOVRB() {
        rb += memory[firstAddr]
        ip += 2
    }
}
