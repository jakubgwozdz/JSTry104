package utils

import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

inline fun <reified K : HTMLElement> Document.byId(elementId: String) =
    getElementById(elementId) as K
