package beveragebandits.browser

import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.js.div
import org.w3c.dom.HTMLParagraphElement
import kotlin.browser.document

fun beverageBanditsInit() {
    val placeholder = document.getElementById("placeholder") as HTMLParagraphElement
    val parent = placeholder.parentElement
    placeholder.remove()
    parent!!.append(container)
}

val container by lazy {
    document.create.div("container-fluid text-monospace d-flex flex-column") {
        h1 { +"Beverage Bandits" }

    }
}