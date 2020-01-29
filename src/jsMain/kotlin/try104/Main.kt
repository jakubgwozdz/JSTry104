package try104

import cryostasis.browser.cryostasisInit
import kotlin.browser.document
import kotlin.browser.window

fun main() {

    println("url: ${document.URL}")
    println("uri: ${document.documentURI}")
    println(document)

    val w: dynamic = window
    w.jstry104 = Any()
    w.jstry104.cryostasisInit = ::cryostasisInit
    w.jstry104.indexInit = ::load
    w.jstry104.indexClick = ::click
}

