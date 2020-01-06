package cryostasis

import kotlinx.coroutines.FlowPreview
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

class CryostasisView(
    val programTextArea: HTMLTextAreaElement,
    val disassemblyPre: HTMLPreElement,
    val statusPre: HTMLPreElement,
    val gameOutputPre: HTMLPreElement,
    val gameInputInput: HTMLInputElement
)

@FlowPreview
val view by lazy {
    with(container.ownerDocument!!) {
        CryostasisView(
            getElementById("program-input") as HTMLTextAreaElement,
            getElementById("disassembly-output") as HTMLPreElement,
            getElementById("intcode-state") as HTMLPreElement,
            getElementById("game-output") as HTMLPreElement,
            getElementById("game-input") as HTMLInputElement
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
        div("input-group input-group-lg") {
            div("input-group-prepend") {
                span("input-group-text") {
                    +"Intcode"
                    br { }
                    +"program"
                }
            }
            textArea(classes = "form-control") {
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
            div("col-2") {
                div {
                    button(type = ButtonType.button, classes = "btn btn-primary btn-block") {
                        +"Run"
                        onClickFunction = ::runIntcode
                    }
                }
                div {
                    div("card card-body") {
                        h5("card-title") {
                            + "Intcode state"
                        }
                        pre("card-text") {
                            +"...."
                            id = "intcode-state"
                        }
                    }
                }
            }
            div("col-10") {
                div("card card-body") {
                    pre("border border-info rounded") {
                        +"...."
                        id = "game-output"
                    }
                    input(type = InputType.text, classes = "form-control text-light bg-secondary border-info") {
                        id = "game-input"
                        onChangeFunction = ::commandEntered
                    }
                }
            }
        }

    }
}