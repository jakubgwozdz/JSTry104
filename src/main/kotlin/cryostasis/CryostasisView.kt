package cryostasis

import kotlinx.coroutines.FlowPreview
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import kotlin.browser.document

class CryostasisView(
    val programTextArea: HTMLTextAreaElement,
    val disassemblyPre: HTMLPreElement,
    val intcodeStatePre: HTMLPreElement,
    val gameOutputPre: HTMLPreElement,
    val gameInputInput: HTMLInputElement,
    val shipScanStatePre: HTMLPreElement
) {
    fun reset() {
        intcodeStatePre.textContent = "...."
        shipScanStatePre.textContent = "...."
        gameOutputPre.textContent = "...."
    }
}

@FlowPreview
val view by lazy {

    inline fun <reified K : HTMLElement> Document.byId(elementId: String) =
        getElementById(elementId) as K

    with(container.ownerDocument!!) {
        CryostasisView(
            byId("program-input"),
            byId("disassembly-output"),
            byId("intcode-state"),
            byId("game-output"),
            byId("game-input"),
            byId("shipscan-state")
        )
    }
}

// build DOM using kontlinx.html fluent builders

@FlowPreview
val container by lazy {
    document.body!!.append.div("container text-monospace") {
        h1 { +"Cryostasis" }
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

        div("row") {
            div("col-4 pr-0") {
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
                        pre("card-text") {
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
                    pre("border border-info rounded p-2") {
                        +"...."
                        id = "game-output"
                    }
                    input(
                        type = InputType.text,
                        classes = "form-control text-light bg-secondary border-info"
                    ) {
                        id = "game-input"
                        onChangeFunction = ::commandEntered
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
