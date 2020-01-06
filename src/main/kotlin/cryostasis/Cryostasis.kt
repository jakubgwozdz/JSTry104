package cryostasis

import intcode.Intcode
import intcode.disassemblyProgram
import intcode.fullLines
import intcode.parseIntcode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

@FlowPreview
fun runIntcode(e: Event) {
    val program = view.programTextArea.value
    val memory = parseIntcode(program)
    val inChannel = Channel<Long>()
    val outChannel = Channel<Long>()
    val computer = Intcode(memory, inChannel, outChannel)

    GlobalScope.launch {
        launch {
            computer.run()
            inChannel.close()
            outChannel.close()
        }

        outChannel.consumeAsFlow()
            .map { it.toInt().toChar() }
            .fullLines()
            .onEach {
                view.gameOutputPre.textContent += it + "\n"
                view.gameOutputPre.scrollTop = view.gameOutputPre.scrollHeight.toDouble()
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

}