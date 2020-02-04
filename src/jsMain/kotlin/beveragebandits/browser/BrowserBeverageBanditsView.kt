package beveragebandits.browser

import beveragebandits.Cavern
import beveragebandits.CombatInProgress
import beveragebandits.ElvesWin
import beveragebandits.EndOfCombat
import beveragebandits.FightRules
import beveragebandits.Mob
import beveragebandits.MobType
import beveragebandits.Phase
import beveragebandits.Position
import beveragebandits.Reporting
import beveragebandits.StartOfCombat
import beveragebandits.reporting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
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
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import utils.byId
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date
import kotlin.math.PI

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
) : Reporting {

    init {

    }

    var start = Date.now()

    override fun combatPhase(phase: Phase) {

        if (phase is StartOfCombat) {
            start = Date.now()
        } else if (phase is EndOfCombat) {
            window.alert(
                "After ${phase.state.roundsCompleted} rounds (${Date.now() - start}ms), " +
                    "outcome is ${phase.state.outcome}, " +
                    "${if (phase is ElvesWin) "Elves" else "Golbins"} won"
            )
        }
        draw(phase)
    }

    private fun draw(phase: Phase) {
        (canvas.getContext("2d") as CanvasRenderingContext2D).run {
            fillStyle = "#111"
            fillRect(1.0, 1.0, 638.0, 638.0)
            phase.state.cavern.map.forEachIndexed { y, line ->
                line.forEachIndexed { x, char ->
                    if (char == '#') drawWall(x, y)
                }
            }
            phase.state.mobs
                .filter { it.hp > 0 }
                .forEach { drawMob(it) }
        }
    }

    private fun CanvasRenderingContext2D.drawMob(mob: Mob) {
        val x = mob.position.x * 20 + 10.0
        val y = mob.position.y * 20 + 10.0
        fillStyle = if (mob.type == MobType.Goblin) "#3f3" else "#f33"
        beginPath()
        ellipse(x, y, 3.0, 3.0, 0.0, 0.0, 2 * PI)
        fill()
        beginPath()
        ellipse(x, y + 4, 2.0, 4.0, 0.0, 0.0, 2 * PI)
        fill()
        fillStyle = "#33f"
        fillRect(x - 8, y - 8, 17.0 * mob.hp / 200.0, 2.0)
        strokeStyle = "#000"
        strokeRect(x - 9, y - 9, 18.0, 4.0)
    }

    private fun CanvasRenderingContext2D.drawWall(x: Int, y: Int) {
        fillStyle = "#fec"
        fillRect(x * 20.0 + 1, y * 20.0 + 1, 18.0, 18.0)
    }

    var job: Job? = null

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
        job?.cancel()
        job = FightRules().run {
            GlobalScope.launch(Dispatchers.Default) {
                var phase: Phase = newCombat(Cavern(cavernTextArea.value))
                while (phase is CombatInProgress) {
                    phase = nextPhase(phase)
                    yield()
                }
                console.log(phase)
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

