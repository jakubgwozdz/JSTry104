package intcode

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

@Suppress("BlockingMethodInNonBlockingContext")
fun Flow<Char>.fullLines(): Flow<String> =
    flow {
        val builder = StringBuilder()
        collect {
            when (it) {
                '\n' -> emit(builder.toString()).also { builder.clear() }
                else -> builder.append(it)
            }
        }
    }

suspend fun SendChannel<Long>.writeln(msg: String) {
    println(msg)
    msg.map { it.toInt().toLong() }.forEach { send(it) }
    send('\n'.toInt().toLong())
}