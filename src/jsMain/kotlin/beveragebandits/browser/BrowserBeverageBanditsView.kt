package beveragebandits.browser

import beveragebandits.Cavern
import beveragebandits.EndOfCombat
import beveragebandits.FightRules
import beveragebandits.Mob
import beveragebandits.Phase
import beveragebandits.Position
import beveragebandits.Reporting
import beveragebandits.StartOfCombat
import beveragebandits.reporting
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.spellCheck
import kotlinx.html.textArea
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.MIDDLE
import org.w3c.dom.events.Event
import utils.byId
import kotlin.browser.document
import kotlin.browser.window
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

fun beverageBanditsInit() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    val parent = placeholder.parentElement
    placeholder.remove()
    parent!!.append(container)

    reporting = view
}

class BrowserBeverageBanditsView(
    private val cavernTextArea: HTMLTextAreaElement,
    private val canvas: HTMLCanvasElement
): Reporting {

    init {

    }

    var start = Date.now()

    override fun combatPhase(phase: Phase) {

        if (phase is StartOfCombat) {
            start = Date.now()
        } else if (phase is EndOfCombat) {
            window.alert("Time was ${Date.now() - start}")
        }

        if (phase is StartOfCombat) {
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            ctx.textAlign = CanvasTextAlign.CENTER
            ctx.textBaseline = CanvasTextBaseline.MIDDLE
            phase.state.cavern.map.forEachIndexed { y, line ->
                line.forEachIndexed { x, char ->
                    ctx.fillText("$char", x * 20.0 + 10, y * 20.0 + 10)
                }
            }
        }
    }

    override fun mobAttacks(
        attacker: Mob,
        target: Mob,
        attackPower: Int
    ) {
        // console.log("Mob $attacker hits $target for $attackPower")
    }

    override fun mobMoves(
        mob: Mob,
        path: List<Position>
    ) {
        // console.log("Mob $mob moves to ${path[1]}")
    }

    fun fight() {
        FightRules().run {
            val s = newCombat(Cavern(cavernTextArea.value))
            GlobalScope.launch {
                val e = fightToEnd(s)
                console.log(e)
            }
        }
    }
}

val container by lazy {
    document.create.div("container text-monospace d-flex flex-column") {
        h1 { +"Beverage Bandits" }
        p {
            +"Download your puzzle input from "
            a("https://adventofcode.com/2018/day/15") { +"Advent of Code 2018 day 15" }
            +" or use mine."
        }
        form {
            div("form-group") {
                label { +"Map input" }
                textArea(classes = "form-control text-light bg-secondary") {
                    +beveragebandits.jakubgwozdz.cavernInput
                    id = "cavern-input"
                    spellCheck = false
                }
            }
            button(type = ButtonType.button, classes = "btn btn-outline-primary") {
                +"Fight!"
                onClickFunction = ::fight
            }
        }
        div("border border-info rounded p-2") {
            canvas {
                id = "map-canvas"
                width = "640"
                height = "640"
            }
        }
    }
}

val view by lazy {
    with(container.ownerDocument!!) {
        BrowserBeverageBanditsView(
            byId("cavern-input"),
            byId("map-canvas")
        )
    }
}

fun fight(e: Event) {
    view.fight()
}

