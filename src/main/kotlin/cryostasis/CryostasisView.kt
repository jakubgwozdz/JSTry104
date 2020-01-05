package cryostasis

import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.HTMLTextAreaElement
import kotlin.browser.document

class CryostasisView(
    val programTextArea: HTMLTextAreaElement,
    val disassemblyParagraph: HTMLPreElement
)

val view by lazy {
    with(container.ownerDocument!!) {
        CryostasisView(
            getElementById("program-input") as HTMLTextAreaElement,
            getElementById("disassembly-output") as HTMLPreElement
        )
    }
}

// build DOM using kontlinx.html fluent builders

val container by lazy {
    document.body!!.append.div("container") {
        h1 { +"Cryostasis" }
        p {
            +"Download your puzzle input from "
            a("https://adventofcode.com/2019/day/25") { +"Advent of Code" }
            +" or use mine"
        }
        div("input-group") {
            div("input-group-prepend text-wrap") {
                span("input-group-text") { +"Intcode program" }
            }
            textArea(classes = "form-control text-monospace") {
                +cryostasis.jakubgwozdz.program
                id = "program-input"
            }
        }
        p { }
        div {
            p {
                button(type = ButtonType.button, classes = "btn btn-primary") {
                    +"Disassembly"
                    attributes["data-toggle"] = "collapse"
                    attributes["data-target"] = "#disassembly-div"
                    onClickFunction = ::disassembly
                }
            }
            div("collapse") {
                id = "disassembly-div"
                div("card card-body") {
                    pre {
                        +"program..."
                        id = "disassembly-output"
                    }
                }
            }
        }
    }
}