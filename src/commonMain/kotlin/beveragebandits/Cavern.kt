package beveragebandits

enum class Direction { N, W, E, S }

data class Position(val y: Int, val x: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int =
        compareValuesBy(this, other, Position::y, Position::x)

    operator fun plus(direction: Direction): Position = when (direction) {
        Direction.N -> y - 1 by x
        Direction.W -> y by x - 1
        Direction.E -> y by x + 1
        Direction.S -> y + 1 by x
    }

    override fun toString(): String = "($y,$x)"

    fun isAdjacentTo(other: Position): Boolean {
        return (this.y == other.y && (this.x == other.x + 1 || this.x == other.x - 1)) ||
            (this.x == other.x && (this.y == other.y + 1 || this.y == other.y - 1))
    }
}

infix fun Int.by(x: Int): Position = Position(this, x)

data class Cavern(internal val map: List<String>) {
    constructor(input: String) : this(input.lines())

    override fun toString(): String = map.joinToString("\n")

    fun emptyAt(position: Position) = this[position] == '.'

    operator fun get(position: Position) =
        when {
            position.y !in (map.indices) -> null
            position.x !in (map[position.y].indices) -> null
            else -> map[position.y][position.x]
        }
}

fun Array<String>.with(position: Position, char: Char) = this.apply {
    this[position.y] = this[position.y].toCharArray()
        .apply { this[position.x] = char }
        .concatToString()
}

fun Cavern.without(position: Position): Cavern {
    check(this[position] in MobType.chars) { "no mob at $position" }
    return Cavern(
        map.toTypedArray()
            .with(position, '.')
            .asList()
    )
}

fun Cavern.mobMove(from: Position, to: Position): Cavern {
    val mobChar = this[from]
    check(mobChar in MobType.chars) { "no mob at $from" }
    check(this[to] == '.') { "not empty at $to" }
    return Cavern(
        map.toTypedArray()
            .with(from, '.')
            .with(to, mobChar!!)
            .asList()
    )
}

fun Cavern.mobMove(from: Position, direction: Direction) = this.mobMove(from, from + direction)

fun Cavern.mobs() = map.indices
    .flatMap { y ->
        map[y].indices
            .filter { x -> map[y][x] in MobType.chars }
            .map { x -> y by x to MobType.of(map[y][x]) }
    }

fun Cavern.waysOut(l: List<Position>): List<Position> = Direction.values()
    .map { l.last() + it }
    .filter { emptyAt(it) }
    .filter { it !in l }
