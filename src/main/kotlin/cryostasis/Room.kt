package cryostasis

data class Room(
    val name: String,
    val description: String,
    val doors: List<Direction>,
    val items: List<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Room) return false

        if (name != other.name) return false
        if (description != other.description) return false
        if (doors != other.doors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + doors.hashCode()
        return result
    }
}

class RoomBuilder {

    private enum class State {
        START, AFTER_NAME, AFTER_DESCRIPTION, AFTER_DOORS_HERE, AFTER_LAST_DOOR, AFTER_ITEMS_HERE, AFTER_LAST_ITEM
    }

    private val name = mutableListOf<String>()
    private val description = mutableListOf<String>()
    private val doors = mutableListOf<Direction>()
    private val items = mutableListOf<String>()
    private var state = State.START

    fun clear() {
        name.clear()
        description.clear()
        doors.clear()
        items.clear()
        state = State.START
    }

    fun accept(line: String) {
        state = when (state) {
            State.START -> when {
                line.isBlank() -> state
                else -> {
                    name += roomNameRegex.matchEntire(line)?.groupValues?.get(1)
                        ?: error("expected name, got '$line'")
                    State.AFTER_NAME
                }
            }
            State.AFTER_NAME -> when {
                line.isBlank() -> State.AFTER_DESCRIPTION
                else -> {
                    description += line
                    state
                }
            }
            State.AFTER_DESCRIPTION -> when (line) {
                "Doors here lead:" -> State.AFTER_DOORS_HERE
                else -> error("expected 'Doors here lead:', got '$line'")
            }
            State.AFTER_DOORS_HERE -> when {
                line.isBlank() -> State.AFTER_LAST_DOOR
                else -> {
                    doors += Regex("- (north|south|west|east)")
                        .matchEntire(line)
                        ?.groupValues
                        ?.get(1)
                        ?.let { Direction.from(it) }
                        ?: error("expected door, got '$line'")
                    state
                }
            }
            State.AFTER_LAST_DOOR -> when (line) {
                "Items here:" -> State.AFTER_ITEMS_HERE
                else -> error("expected 'Items here:' or end of description, got '$line'")
            }

            State.AFTER_ITEMS_HERE -> when {
                line.isBlank() -> State.AFTER_LAST_ITEM
                else -> {
                    items += Regex("- (.*)").matchEntire(line)?.groupValues?.get(1)
                        ?: error("expected door, got '$line'")
                    state
                }
            }
            State.AFTER_LAST_ITEM -> TODO()
        }
    }

    fun build(): Room = when (state) {
        State.AFTER_LAST_DOOR, State.AFTER_LAST_ITEM -> {
            Room(
                name.single(),
                description.joinToString("\n"),
                doors.toList(),
                items.toList()
            )
                .also { clear() }
        }
        else -> error("Unexpected build() in state $state")
    }
}
