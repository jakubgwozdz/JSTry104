package utils

fun <E> List<E>.replace(prev: E, now: E): List<E> {
    val i = this.indexOf(prev)
    return if (i < 0) this
    else this.subList(0, i) + now + this.subList(i + 1, this.size)
}

fun <E> List<E>.without(prev: E): List<E> {
    val i = this.indexOf(prev)
    return if (i < 0) this
    else this.subList(0, i) + this.subList(i + 1, this.size)
}