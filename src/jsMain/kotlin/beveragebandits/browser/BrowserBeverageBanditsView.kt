package beveragebandits.browser

import beveragebandits.Cavern
import beveragebandits.CombatInProgress
import beveragebandits.CombatState
import beveragebandits.ElvesWin
import beveragebandits.FightRules
import beveragebandits.GoblinsWin
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
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.js.div
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.p
import kotlinx.html.select
import kotlinx.html.spellCheck
import kotlinx.html.textArea
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLLabelElement
import org.w3c.dom.HTMLParagraphElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.Event
import utils.byId
import kotlin.browser.document
import kotlin.js.Date
import kotlin.math.PI

fun beverageBanditsInit() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    val parent = placeholder.parentElement
    placeholder.remove()
    parent!!.append(container)

    reporting = view
}

val scale = 748.0 / 32

class BrowserBeverageBanditsView(
    private val cavernTextArea: HTMLTextAreaElement,
    private val canvas: HTMLCanvasElement,
    private val elvesApInput: HTMLInputElement,
    private val goblinsWinConditionInput: HTMLSelectElement,
    private val timeLabel: HTMLLabelElement,
    private val framerateLabel: HTMLLabelElement,
    private val outcomeLabel: HTMLLabelElement
) : Reporting {

    var start = Date.now()
    var frames = 0

    override fun combatPhase(phase: Phase) {

        if (phase is StartOfCombat) {
            start = Date.now()
            frames = 0
        } else {
            frames++
            val time = Date.now() - start
            timeLabel.textContent = "$time ms"
            framerateLabel.textContent = "${(frames*1000/(time+1)).toInt()} fps"
            outcomeLabel.textContent = "${phase.state.outcome}"
            if (phase is ElvesWin) outcomeLabel.textContent+=", Elves won"
            if (phase is GoblinsWin) outcomeLabel.textContent+=", Goblins won"

        }
        draw(phase)
    }

    private fun draw(phase: Phase) {
        (canvas.getContext("2d") as CanvasRenderingContext2D).run {
            fillStyle = "#111"
            fillRect(1.0, 1.0, 32 * scale - 2.0, 32 * scale - 2.0)
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
        val x = mob.position.x * scale + scale / 2
        val y = mob.position.y * scale + scale / 2
        fillStyle = if (mob.type == MobType.Goblin) "#3f3" else "#f33"
        beginPath()
        ellipse(x, y, 0.15 * scale, 0.15 * scale, 0.0, 0.0, 2 * PI)
        fill()
        beginPath()
        ellipse(x, y + 0.2 * scale, 0.1 * scale, 0.2 * scale, 0.0, 0.0, 2 * PI)
        fill()
        fillStyle = "#33f"
        fillRect(x - scale / 2 + 2, y - scale / 2 + 2, (scale - 3) * mob.hp / 200.0, scale / 10)
        strokeStyle = "#555"
        strokeRect(x - scale / 2 + 1, y - scale / 2 + 1, scale - 2, scale / 10 + 2)
    }

    private fun CanvasRenderingContext2D.drawWall(x: Int, y: Int) {
        fillStyle = "#fec"
        fillRect(x * scale + 1, y * scale + 1, scale - 2, scale - 2)
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

    override fun mobStops(
        mob: Mob
    ) {
        // console.log("Mob $mob moves to ${path[1]}")
    }

    val allElvesDie: (CombatState) -> Boolean = { s -> s.mobs.none { it.type == MobType.Elf && it.hp > 0 } }
    val anyElfDies: (CombatState) -> Boolean = { s -> s.mobs.any { it.type == MobType.Elf && it.hp <= 0 } }

    fun fight() {
        job?.cancel()

        val condition = goblinsWinConditionInput.value.toLowerCase()
        val goblinsWin: (CombatState) -> Boolean = if (condition.startsWith("all")) {
            allElvesDie
        } else {
            anyElfDies
        }
        val elvesAttackPower = elvesApInput.value.toInt()
        job = FightRules(elvesAttackPower, goblinsWin).run {
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
    document.create.div("container-md d-flex flex-column") {
        h1 { +"Beverage Bandits" }
        p {
            +"Download your puzzle input from "
            a("https://adventofcode.com/2018/day/15") { +"Advent of Code 2018 day 15" }
            +" or use mine."
        }
        form {
            onSubmitFunction = ::fight
            div("form-row pd-5") {
                div("form-group col-md-6") {
                    label {
                        htmlFor = "cavern-input"
                        +"Map input"
                    }
                    textArea(classes = "form-control text-monospace text-light bg-secondary") {
                        +beveragebandits.jakubgwozdz.cavernInput
                        id = "cavern-input"
                        spellCheck = false
                    }
                }
                div("col-md-6") {
                    div("form-group") {
                        label {
                            htmlFor = "elves-ap-input"
                            +"Elves Attack Power"
                        }
                        input(InputType.number, classes = "form-control text-light bg-secondary") {
                            value = "3"
                            id = "elves-ap-input"
                        }
                    }
                    div("form-group") {
                        label {
                            htmlFor = "goblins-win-condition"
                            +"Goblins Win Condition"
                        }
                        select("form-control text-light bg-secondary") {
                            id = "goblins-win-condition"
                            option {
                                selected = true
                                value = "all"
                                +"All elves die"
                            }
                            option {
                                value = "any"
                                +"Any elf dies"
                            }
                        }
                    }
                    button(type = ButtonType.button, classes = "btn btn-outline-primary") {
                        +"Fight!"
                        onClickFunction = ::fight
                    }
                }
            }
        }
        div("row border border-info rounded p-2 mu-5") {
            canvas("col-md-8") {
                id = "map-canvas"
                width = "${scale * 32}"
                height = "${scale * 32}"
            }
            div("col-md-4") {
                div("form-group") {
                    label {
                        htmlFor = "time-output"
                        +"Time"
                    }
                    label("form-control text-light bg-secondary") {
                        id = "time-output"
                    }
                }
                div("form-group") {
                    label {
                        htmlFor = "framerate-output"
                        +"Framerate"
                    }
                    label("form-control text-light bg-secondary") {
                        id = "framerate-output"
                    }
                }
                div("form-group") {
                    label {
                        htmlFor = "outcome-output"
                        +"Outcome"
                    }
                    label("form-control text-light bg-secondary") {
                        id = "outcome-output"
                    }
                }
            }
        }
    }
}

val view by lazy {
    with(container.ownerDocument!!) {
        BrowserBeverageBanditsView(
            byId("cavern-input"),
            byId("map-canvas"),
            byId("elves-ap-input"),
            byId("goblins-win-condition"),
            byId("time-output"),
            byId("framerate-output"),
            byId("outcome-output")
        )
    }
}

fun fight(e: Event) {
    view.fight()
    e.preventDefault()
}

