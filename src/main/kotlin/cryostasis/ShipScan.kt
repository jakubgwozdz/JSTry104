package cryostasis

enum class Direction(val text: String) {

    N("north"), E("east"), S("south"), W("west");

    val back: Direction
        get() = when (this) {
            N -> S
            E -> W
            S -> N
            W -> E
        }

    val left: Direction
        get() = when (this) {
            N -> W
            E -> N
            S -> E
            W -> S
        }

    val right: Direction
        get() = when (this) {
            N -> E
            E -> S
            S -> W
            W -> N
        }

    companion object {
        fun from(s: String): Direction = values().single { it.text == s }
    }
}

enum class WeightState { UNKNOWN, TOO_HEAVY, TOO_LIGHT, OK }

data class SearchState(
    val knownRooms: MutableMap<String, Room> = mutableMapOf(),
    val knownExits: MutableMap<String, MutableMap<Direction, String>> = mutableMapOf(),
    val movements: MutableList<Pair<String, Direction>> = mutableListOf(),
    var currentRoomId: String? = null,
    var lastMovement: Direction? = null,
    val knownDirectionsToPlaces: MutableMap<String, List<Pair<String, Direction>>> = mutableMapOf(),
    var weightState: WeightState = WeightState.UNKNOWN,
    val inventory: MutableList<String> = mutableListOf(),
    var result: String? = null
)


class SearchUpdater(val state: SearchState) {

    fun update(output: Output) = when (output) {
        is RoomDescription -> room(output)
        is RoomWithTeleportDescription -> teleport(output)
        is TakeActionDescription -> itemTaken(output)
        is DropActionDescription -> itemDrop(output)
        is Prompt -> error("'Command?' not allowed here")
        is Success -> state.result = output.code
    }

    private fun itemTaken(output: TakeActionDescription) {
        val item = output.item
        state.inventory += item
        state.knownRooms.compute(state.currentRoomId!!) { _, room -> room!!.copy(items = room.items - item) }
    }

    private fun itemDrop(output: DropActionDescription) {
        val item = output.item
        state.inventory -= item
        state.knownRooms.compute(state.currentRoomId!!) { _, room -> room!!.copy(items = room.items + item) }
    }

    private fun teleport(description: RoomWithTeleportDescription) {
        val room = description.room
        updateMap(room)
        state.lastMovement = null
        state.movements.clear()
        state.movements.addAll(state.knownDirectionsToPlaces[room.name]!!)
        state.movements.removeLast()
        val cause = alertRegex.matchEntire(description.reason)!!.groupValues[1]
        state.weightState = when (cause) {
            "heavier" -> WeightState.TOO_LIGHT
            "lighter" -> WeightState.TOO_HEAVY
            else -> error(cause)
        }
    }

    private fun room(description: RoomDescription) {
        val room = description.room
        updateMap(room)
    }

    private fun updateMap(room: Room) {
        val prevRoomId = state.currentRoomId

        val visitedAlready =
            state.knownRooms[room.name]?.also { if (it != room) error("this room was $it, now it's $room") }
        state.knownRooms[room.name] = room
        state.currentRoomId = room.name

        if (prevRoomId != null && state.lastMovement != null) {
            state.knownExits.computeIfAbsent(prevRoomId) { mutableMapOf() }
                ?.set(state.lastMovement!!, room.name)
        }
        state.knownDirectionsToPlaces.computeIfAbsent(room.name) {
            state.movements.toList().compact { it.first }
        }
    }
}

private fun <E> MutableList<E>.removeLast() {
    removeAt(size - 1)
}

private fun <K, V : Any> MutableMap<K, V>.compute(key: K, op: (K, V?) -> V?) =
    op(key, this[key]).also { if (it != null) this[key] = it else this.remove(key) }

private fun <K, V : Any> MutableMap<K, V>.computeIfAbsent(key: K, op: (K) -> V?) =
    this[key] ?: op(key)?.also { this[key] = it }

private fun <K, V : Any> MutableMap<K, V>.compute(key: K, op: (K, V?) -> V) =
    op(key, this[key]).also { this[key] = it }

private fun <K, V : Any> MutableMap<K, V>.computeIfAbsent(key: K, op: (K) -> V) =
    this[key] ?: op(key).also { this[key] = it }

private fun <E, T> List<E>.compact(discriminatorOp: (E) -> T): List<E> {
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
