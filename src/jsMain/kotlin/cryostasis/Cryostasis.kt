package cryostasis

import intcode.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.events.Event
import kotlin.browser.document

var decompiledProgram: String = ""

fun disassembly(e: Event) {
    val program = view.programTextArea.value
    if (program != decompiledProgram) {
        val memory = parseIntcode(program)
        val length = "; Program length in chars: ${program.length}"
        val size = "; Program length in memory: ${memory.size}"
        val disassembly = disassemblyProgram(program)
        view.disassemblyPre.innerText = (sequenceOf(length, size) + disassembly).joinToString("\n")
    }
    decompiledProgram = program
}

var intcodeProcess: IntcodeProcess? = null

data class IntcodeProcess(
    val computer: Intcode,
    val inChannel: Channel<Long>,
    val outChannel: Channel<Long>,
    val job: Job,
    val searchState: SearchState
)

val stateUpdater = SearchUpdater()
val shipScan = ShipScan()

@FlowPreview
fun runIntcode(e: Event) {

    intcodeProcess?.job?.cancel()
    autoscanOff()
    view.reset()

    val program = view.programTextArea.value
    val memory = parseIntcode(program)
    val inChannel = Channel<Long>()
    val outChannel = Channel<Long>()
    val computer = Intcode(memory, inChannel, outChannel, debug = ::debugger)

    (e.currentTarget as HTMLElement?)?.innerText = "Restart"

    val state = SearchState()

    val job = GlobalScope.launch {
        launch {
            try {
                computer.run()
                println("Intcode finished")
            } catch (e: Exception) {
                println(e)
                throw e;
            } finally {
                inChannel.close()
                outChannel.close()
            }
        }

        outChannel.consumeAsFlow()
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach { view.println(it) }
            .outputs()
            .onEach {
                when (it) {
                    is Prompt -> checkInput()
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

var autoscan = false

fun autoscanOn(e: Event? = null) {
    autoscan = true
    checkInput()
}

fun autoscanOff(e: Event? = null) {
    autoscan = false
    view.autoScanButton.classList.remove("active")
}

fun checkInput() {
    println("checking input, $autoscan")
    val state = intcodeProcess?.searchState
    if (autoscan && state != null) {
        shipScan.moveToNextUnknown(state)
            ?.let { sendCommand(it) }
            ?: autoscanOff()
    } else {
        view.gameInputInput.focus()
    }
}

@FlowPreview
fun commandEntered(e: Event) {
    val command = view.gameInputInput.value.trim()
        .let { if (it.isNotBlank()) it[0].toLowerCase() + it.substring(1) else it }
    sendCommand(command)
}

private fun sendCommand(command: String) {
    view.println(command)
    view.gameInputInput.value = ""
    GlobalScope.launch {
        intcodeProcess?.inChannel?.writeln(command)
        Direction.values()
            .singleOrNull { it.text == command }
            ?.let { direction ->
                intcodeProcess?.let { stateUpdater.moving(it.searchState, direction) }
            }
    }
}

@FlowPreview
fun debugger(computer: Intcode) {
    view.intcodeStatePre.textContent = """
        |ip: ${computer.ip.toString().padEnd(6)} rb: ${computer.rb}
        |${dissassembly(computer.memory, computer.ip)}
    """.trimMargin()
}

