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

@FlowPreview
fun load() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    placeholder.remove()
    view // just to make sure it's lazily loaded
//    println(view.programTextArea.textContent)
}

var decompiledProgram: String = ""

@FlowPreview
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


@FlowPreview
fun runIntcode(e: Event) {

    intcodeProcess?.job?.cancel()
    view.reset()

    val program = view.programTextArea.value
    val memory = parseIntcode(program)
    val inChannel = Channel<Long>()
    val outChannel = Channel<Long>()
    val computer = Intcode(memory, inChannel, outChannel, debug = ::debugger)

    (e.currentTarget as HTMLElement?)?.innerText = "Restart"

    val state = SearchState()
    val stateUpdater = SearchUpdater(state)

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
            .onEach {
                view.gameOutputPre.textContent += it + "\n"
                view.gameOutputPre.scrollTop = view.gameOutputPre.scrollHeight.toDouble()
            }
            .outputs()
            .onEach {
                when (it) {
                    is Prompt -> view.gameInputInput.focus()
                    else -> {
                        stateUpdater.update(it)
                        view.shipScanStatePre.textContent = formatState(state)
                    }
                }
            }
            .collect()
    }

    intcodeProcess = IntcodeProcess(computer, inChannel, outChannel, job, state)

}

@FlowPreview
fun commandEntered(e: Event) {
    val command = view.gameInputInput.value
    view.gameOutputPre.textContent += "$command\n"
    view.gameInputInput.value = ""
    GlobalScope.launch {
        intcodeProcess?.inChannel?.writeln(command)
        Direction.values()
            .filter { it.text == command }
            .forEach { intcodeProcess?.searchState?.lastMovement = it }
    }
}

@FlowPreview
fun debugger(computer: Intcode) {
    view.intcodeStatePre.textContent = """ip: ${computer.ip}
        |rb: ${computer.rb}
        |${dissassembly(computer.memory, computer.ip)}
    """.trimMargin()
}

fun formatState(state: SearchState) = buildString {
    state.knownRooms.forEach { (roomId, room) ->
        append("- $roomId\n")
        val knownExits = state.knownExits[roomId] ?: mutableMapOf()
        room.doors.forEach {
            append(" `- $it - ${knownExits[it] ?: "???"}\n")
        }
    }
}
