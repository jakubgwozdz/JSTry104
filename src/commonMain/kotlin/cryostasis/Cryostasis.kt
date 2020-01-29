package cryostasis

import intcode.Intcode
import intcode.fullLines
import intcode.parseIntcode
import intcode.writeln
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

interface CryostasisView {
    fun reset()
    fun println(line: String)
    fun displayState(state: SearchState)
    fun debugger(computer: Intcode)
    fun readCommand(): String
    fun getProgram(): String
    fun checkInput()
}

var intcodeProcess: IntcodeProcess? = null
var cryostasisView: CryostasisView? = null

data class IntcodeProcess(
    val computer: Intcode,
    val inChannel: Channel<Long>,
    val outChannel: Channel<Long>,
    val job: Job,
    val searchState: SearchState
)

val stateUpdater = SearchUpdater()
val shipScan = ShipScan()

fun runIntcode(program: String, view: CryostasisView) {

    intcodeProcess?.job?.cancel()
    cryostasisView = view
    view.reset()

    val memory = parseIntcode(program)
    val inChannel = Channel<Long>()
    val outChannel = Channel<Long>()
    val computer = Intcode(memory, inChannel, outChannel, debug = view::debugger)

    val state = SearchState()

    val job = GlobalScope.launch {
        launch {
            try {
                computer.run()
                println("Intcode finished")
            } catch (e: Exception) {
                println(e)
                throw e
            } finally {
                inChannel.close()
                outChannel.close()
            }
        }

        outChannel.consumeAsFlow()
            .buffer()
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach { view.println(it) }
            .outputs()
            .onEach {
                when (it) {
                    is Prompt -> view.checkInput()
                    else -> {
                        stateUpdater.update(state, it)
                        view.displayState(state)
                    }
                }
            }
            .collect()
    }

    intcodeProcess =
        IntcodeProcess(computer, inChannel, outChannel, job, state)
}

fun nextAutomaticCommand(): String? = intcodeProcess?.searchState?.let {
    shipScan.moveToNextUnknown(it)
}

fun sendCommand(command: String) {
    cryostasisView!!.println(command)
    GlobalScope.launch {
        intcodeProcess?.inChannel?.writeln(command)
        Direction.values()
            .singleOrNull { it.text == command }
            ?.let { direction ->
                intcodeProcess?.let { stateUpdater.moving(it.searchState, direction) }
            }
    }
}

