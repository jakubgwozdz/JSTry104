package cryostasis

import intcode.disassemblyProgram
import intcode.parseIntcode
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.events.Event
import kotlin.browser.document

fun load() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    placeholder.remove()
    println(view.programTextArea.textContent)
}

var decompiledProgram: String = ""

fun disassembly(e: Event) {
    val program = view.programTextArea.value
    if (program != decompiledProgram) {
        val memory = parseIntcode(program)
        val length = "; Program length in chars: ${program.length}"
        val size = "; Program length in memory: ${memory.size}"
        val disassembly = disassemblyProgram(program)
        view.disassemblyParagraph.innerText = (sequenceOf(length, size) + disassembly).joinToString("\n")
    }
    decompiledProgram = program
}

