package cryostasis

fun <E> MutableList<E>.removeLast() {
    removeAt(size - 1)
}

//fun <K, V : Any> MutableMap<K, V>.compute(key: K, op: (K, V?) -> V?) =
//    op(key, this[key]).also { if (it != null) this[key] = it else this.remove(key) }
//
//fun <K, V : Any> MutableMap<K, V>.computeIfAbsent(key: K, op: (K) -> V?) =
//    this[key] ?: op(key)?.also { this[key] = it }
//
fun <K, V : Any> MutableMap<K, V>.compute(key: K, op: (K, V?) -> V) =
    op(key, this[key]).also { this[key] = it }

fun <K, V : Any> MutableMap<K, V>.computeIfAbsent(key: K, op: (K) -> V) =
    this[key] ?: op(key).also { this[key] = it }

fun <E, T> List<E>.compact(discriminatorOp: (E) -> T): List<E> {
    val result = this.toList()

    val loops = this.groupBy(discriminatorOp)
        .mapValues { (_, v) -> v.count() }
        .filterValues { it > 1 }
        .keys
    val i = indexOfFirst { discriminatorOp(it) in loops }
    if (i < 0) return result

    val j = indexOfLast { discriminatorOp(it) == discriminatorOp(this[i]) }
    return (result.subList(0, i) + result.subList(j, this.size)).compact(discriminatorOp)
}