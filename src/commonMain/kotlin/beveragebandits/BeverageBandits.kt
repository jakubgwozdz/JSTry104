package beveragebandits

enum class Direction { N, W, E, S }

data class Position(val y: Int, val x: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int = compareValuesBy(this, other, Position::y, Position::x)
    operator fun plus(direction: Direction): Position = when (direction) {
        Direction.N -> y - 1 by x
        Direction.W -> y by x - 1
        Direction.E -> y by x + 1
        Direction.S -> y + 1 by x
    }
}

infix fun Int.by(x: Int): Position = Position(this, x)

data class Cavern(val map: List<String>) {
    constructor(input: String) : this(input.lines())
    fun canGoTo(position: Position) = position.y in (0..map.le
}

enum class MobType { E, G }

data class Mob(val x: Int, val y: Int, val type: MobType, val hp: Int = 200)

data class State(
    val cavern: Cavern,
    val elves: List<Mob>, val goblins: List<Mob>,
    val turnsCompleted: Int,
    val mobsToGo: List<Mob>
)