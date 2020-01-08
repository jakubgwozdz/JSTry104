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


class SearchUpdater {

    fun update(state: SearchState, output: Output) = when (output) {
        is RoomDescription -> room(state, output)
        is RoomWithTeleportDescription -> teleport(state, output)
        is TakeActionDescription -> itemTaken(state, output)
        is DropActionDescription -> itemDrop(state, output)
        is Prompt -> error("'Command?' not allowed here")
        is Success -> state.result = output.code
    }

    private fun itemTaken(state: SearchState, output: TakeActionDescription) {
        val item = output.item
        state.inventory += item
        state.knownRooms.compute(state.currentRoomId!!) { _, room -> room!!.copy(items = room.items - item) }
    }

    private fun itemDrop(state: SearchState, output: DropActionDescription) {
        val item = output.item
        state.inventory -= item
        state.knownRooms.compute(state.currentRoomId!!) { _, room -> room!!.copy(items = room.items + item) }
    }

    private fun teleport(state: SearchState, description: RoomWithTeleportDescription) {
        val room = description.room
        updateMap(state, room)
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

    private fun room(state: SearchState, description: RoomDescription) {
        val room = description.room
        updateMap(state, room)
    }

    private fun updateMap(state: SearchState, room: Room) {
        val prevRoomId = state.currentRoomId

        val visitedAlready =
            state.knownRooms[room.name]?.also { if (it != room) error("this room was $it, now it's $room") }
        state.knownRooms[room.name] = room
        state.currentRoomId = room.name

        if (prevRoomId != null && state.lastMovement != null) {
            state.knownExits.computeIfAbsent(prevRoomId) { mutableMapOf() }[state.lastMovement!!] = room.name
        }
        state.knownDirectionsToPlaces.computeIfAbsent(room.name) {
            state.movements.toList().compact { it.first }
        }
    }

    fun move(state: SearchState, direction: Direction) {
        val shortcut = state.movements.toList().compact { it.first }
        if (shortcut.size < state.movements.size) {
            state.movements.clear()
            state.movements.addAll(shortcut)
        }
        state.movements += state.currentRoomId!! to direction
    }
}

class ShipScan {

//    fun moveToNextUnknown()

}