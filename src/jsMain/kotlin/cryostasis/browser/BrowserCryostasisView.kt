package cryostasis.browser

import cryostasis.*
import intcode.Intcode
import intcode.disassemblyProgram
import intcode.dissassembly
import intcode.parseIntcode
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.html.js.div
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import org.w3c.dom.events.Event
import kotlin.browser.document

fun cryostasisInit() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    val parent = placeholder.parentElement
    placeholder.remove()
    parent!!.append(container)
//    println(view.programTextArea.textContent)
}

class BrowserCryostasisView(
    private val programTextArea: HTMLTextAreaElement,
    private val disassemblyPre: HTMLPreElement,
    private val intcodeStatePre: HTMLPreElement,
    private val gameOutputPre: HTMLPreElement,
    private val gameInputInput: HTMLInputElement,
    private val shipScanStatePre: HTMLPreElement,
    private val autoScanButton: HTMLButtonElement
): CryostasisView {

    override fun reset() {
        autoscanOff()
        intcodeStatePre.textContent = "...."
        shipScanStatePre.textContent = "...."
        gameOutputPre.textContent = "...."
    }

    override fun println(line: String) {
        gameOutputPre.textContent += line + "\n"
        gameOutputPre.scrollTop = gameOutputPre.scrollHeight.toDouble()
    }

    override fun displayState(state: SearchState) {
        shipScanStatePre.textContent = formatState(state)
    }

    override fun debugger(computer: Intcode) {
        intcodeStatePre.textContent = """
        |ip: ${computer.ip.toString().padEnd(6)} rb: ${computer.rb}
        |${dissassembly(computer.memory, computer.ip)}
    """.trimMargin()
    }

    override fun readCommand(): String {
        val command = gameInputInput.value.trim()
            .let { if (it.isNotBlank()) it[0].toLowerCase() + it.substring(1) else it }
        gameInputInput.value = ""
        return command
    }

    override fun getProgram(): String = programTextArea.value

    override fun checkInput() {
        if (autoscan) {
            nextAutomaticCommand()
                ?.let { sendCommand(it) }
                ?: autoscanOff()
        } else {
            view.gameInputInput.focus()
        }
    }

    private fun formatState(state: SearchState) = buildString {
        state.knownRooms.forEach { (roomId, room) ->
            val isCurrent = roomId == state.currentRoomId
            val directions = state.knownDirectionsToPlaces[roomId]?.map { it.second }
            val itemsCount = if (room.items.isNotEmpty()) "- ${room.items.size} item(s): ${room.items}" else ""

            append(if (isCurrent) "*" else "-")
            append(" $directions $roomId $itemsCount\n")

            val knownExits = state.knownExits[roomId] ?: mutableMapOf()
            room.doors.forEach {
                append(
                    " `- $it ${knownExits[it] ?: "???"}\n"
                )
            }
        }
    }

    fun disassembly() {
        val program = getProgram()
        if (program != decompiledProgram) {
            val memory = parseIntcode(program)
            val length = "; Program length in chars: ${program.length}"
            val size = "; Program length in memory: ${memory.size}"
            val disassembly = disassemblyProgram(program)
            disassemblyPre.innerText = (sequenceOf(length, size) + disassembly).joinToString("\n")
        }
        decompiledProgram = program
    }

    var decompiledProgram: String = ""

    var autoscan = false

    fun autoscanOn() {
        autoscan = true
        checkInput()
    }

    fun autoscanOff() {
        autoscan = false
        autoScanButton.classList.remove("active")
    }

}

val view by lazy {

    inline fun <reified K : HTMLElement> Document.byId(elementId: String) =
        getElementById(elementId) as K

    with(container.ownerDocument!!) {
        BrowserCryostasisView(
            byId("program-input"),
            byId("disassembly-output"),
            byId("intcode-state"),
            byId("game-output"),
            byId("game-input"),
            byId("shipscan-state"),
            byId("auto-scan")
        )
    }
}

// build DOM using kontlinx.html fluent builders

val container by lazy {
    document.create.div("container-fluid text-monospace  d-flex flex-column") {
        h1 { +"Cryostasis v0.9" }
        p {
            +"Download your puzzle input from "
            a("https://adventofcode.com/2019/day/25") { +"Advent of Code 2019 day 25" }
            +" or use mine."
        }
        div("input-group") {
            div("input-group-prepend") {
                span("input-group-text") {
                    +"Intcode"
                    br { }
                    +"program"
                }
            }
            textArea(classes = "form-control text-light bg-secondary") {
                +cryostasis.jakubgwozdz.program
                id = "program-input"
            }
            div("input-group-append") {
                button(type = ButtonType.button, classes = "btn btn-outline-secondary") {
                    +"Disassembly"
                    br { }
                    +"(optional)"
                    attributes["data-toggle"] = "collapse"
                    attributes["data-target"] = "#disassembly-div"
                    onClickFunction = ::disassembly
                }
            }
        }
        p { }
        div("collapse") {
            id = "disassembly-div"
            div("card card-body") {
                pre {
                    id = "disassembly-output"
                }
            }
        }

        p { }

        div("row flex-grow-1") {
            div("col-4 pr-0 flex-grow-1") {
                div {
                    button(type = ButtonType.button, classes = "btn btn-primary btn-block") {
                        +"Run"
                        onClickFunction = ::runIntcode
                    }
                }
                div {
                    div("card card-body") {
                        h5("card-title") {
                            +"Intcode state"
                        }
                        pre("card-text flex-grow-1") {
                            +"...."
                            id = "intcode-state"
                        }
                    }
                }
                div("mt-2") {
                    div("card card-body") {
                        h5("card-title") {
                            +"Ship scan state"
                        }
                        pre("card-text") {
                            +"...."
                            id = "shipscan-state"
                        }
                    }
                }
            }
            div("col-8") {
                div("card card-body") {
                    pre("border border-info rounded p-2 d-flex flex-column") {
                        +"...."
                        id = "game-output"
                    }
                    div("input-group") {
                        input(
                            type = InputType.text,
                            classes = "form-control text-light bg-secondary border-info"
                        ) {
                            id = "game-input"
                            onChangeFunction = ::commandEntered
                        }
                        div("input-group-append") {
                            button(type = ButtonType.button, classes = "btn btn-outline-info") {
                                +"Scan for me instead"
                                attributes["data-toggle"] = "buttons"
                                id = "auto-scan"
                                onClickFunction = ::autoScan
                            }
                        }
                    }
                }
                div("card card-body small text-light") {
                    h5("card-title") {
                        +"Commands:"
                    }
                    p("card-text") {
                        +"- north, east, south, west ; movement"
                        br {}
                        +"- take <name of item>      ; take an item"
                        br {}
                        +"- drop <name of item>      ; leave an item"
                        br {}
                        +"- inv                      ; display inventory"
                    }
                }
            }
        }

    }
}

fun disassembly(e: Event) {
    view.disassembly()
}

fun runIntcode(e: Event) {
    val program = view.getProgram()
    runIntcode(program, view)
    (e.currentTarget as HTMLElement?)?.innerText = "Restart"
}

fun commandEntered(e: Event?) {
    val command = view.readCommand()
    sendCommand(command)
}

fun autoScan(e: Event?) {
    view.autoscanOn()
}
