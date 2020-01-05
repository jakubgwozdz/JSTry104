package try104

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import try104.somepackage.SomeData
import kotlin.browser.document
import kotlin.coroutines.CoroutineContext
import kotlin.dom.createElement

fun click() = TryNo104().click()

fun load() {
    println("load()")
    coroutinesDiv.firstElementChild!!.remove()

    (1..6).forEach { increment ->
        val div = coroutinesDiv.appendChild(document.createElement("div"))

        val inputs = (1..7)
            .map {
                document.createElement("input") {
                    (this as HTMLInputElement).value = "$it"
                    id = "box-$increment-$it"
                } as HTMLInputElement
            }
            .onEach {
                it.type = "text"
                it.classList.add("coroutines-text")
            }
            .onEach { div.appendChild(it) }
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

class TryNo104CoroutineScope(val inputs: List<HTMLInputElement>, val increment: Int) : CoroutineScope {

    fun start() {
        launch {
            delay(increment * 330L)
            var i = 0
            while (true) {
                inputs.forEach { it.classList.remove("increment-$increment") }
                inputs[i % inputs.size].classList.add("increment-$increment")
                i = i + increment
                delay(1000 - increment * 100L)
            }
        }
    }

    var job = Job()

    override val coroutineContext: CoroutineContext
        get() = job
}