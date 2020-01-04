package try104

import org.w3c.dom.HTMLInputElement
import try104.somepackage.SomeData
import kotlin.browser.document

fun click() = TryNo104().click()

fun load() {
    println("load()")
}

val input by lazy { document.getElementById("in")!! as HTMLInputElement }
val output by lazy { document.getElementById("out")!! as HTMLInputElement }

class TryNo104 {

    fun test() = SomeData("*").data

    fun click() {
        output.value = input.value.map { "$it" }.joinToString(test(), "<", ">")
    }

}