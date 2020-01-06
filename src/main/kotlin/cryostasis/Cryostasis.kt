package cryostasis

import intcode.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.w3c.dom.HTMLButtonElement
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

var intcodeProcess : IntcodeProcess? = null

data class IntcodeProcess(
    val computer: Intcode,
    val inChannel: Channel<Long>,
    val outChannel: Channel<Long>,
    val job: Job
)


@FlowPreview
fun runIntcode(e: Event) {

    intcodeProcess?.job?.cancel()

    val program = view.programTextArea.value
    val memory = parseIntcode(program)
    val inChannel: Channel<Long> = Channel<Long>()
    val outChannel = Channel<Long>()
    val computer = Intcode(memory, inChannel, outChannel)

    (e.currentTarget as HTMLElement?)?.innerText = "Restart"

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
                if (it == "Command?") view.gameInputInput.focus()
            }
//            .outputs()
//            .collect {
//                when (it) {
//                    is Prompt -> inChannel.writeln(decisionMaker.makeDecision())
//                    else -> stateUpdater.update(it)
//                }
//            }
            .collect()
    }

    intcodeProcess = IntcodeProcess(computer, inChannel, outChannel, job)

}

@FlowPreview
fun commandEntered(e:Event) {
    val command = view.gameInputInput.value
    view.gameInputInput.value = ""
    GlobalScope.launch {
        intcodeProcess?.inChannel?.writeln(command)
    }
}