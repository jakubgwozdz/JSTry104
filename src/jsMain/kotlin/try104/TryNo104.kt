package try104

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.dom.append
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.input
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import try104.somepackage.SomeData
import kotlin.browser.document
import kotlin.coroutines.CoroutineContext

fun click() = TryNo104().click()

fun load() {
    println("load()")
    coroutinesDiv.firstElementChild!!.remove()

    val text = "Coroutines are more fun than television!!!".reversed().iterator()

    (1..6).forEach { increment ->
        val div = coroutinesDiv.append.div { }

        val inputs = (1..7)
            .map {
                div.append.input(type = InputType.text) {
                    value = "${text.nextChar()}"
                    id = "box-$increment-$it"
                    classes = setOf("coroutines-text", "increment-$increment")
                }.also {
                    it.style.opacity = "1%"
                }
            }
        TryNo104CoroutineScope(inputs, increment).start()
    }

}

val input by lazy { document.getElementById("in")!! as HTMLInputElement }
val output by lazy { document.getElementById("out")!! as HTMLInputElement }

val coroutinesDiv by lazy { document.getElementById("coroutinesDiv")!! as HTMLDivElement }

class TryNo104 {

    fun test() = SomeData(", ").data

    fun click() {
        output.value = input.value.map { "$it" }.joinToString(test(), "<", ">")
    }

}

class TryNo104CoroutineScope(val inputs: List<HTMLElement>, val increment: Int) : CoroutineScope {

    fun start() {
        launch {
            var i = 6
            val times = 20 + 7 * increment
            while (true) {
                repeat(times) {
                    inputs[i % inputs.size].style.opacity = "${100-(100*it)/times}%"
                    delay(16L)
                }
                i += 6
            }
        }
    }

    var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job
}