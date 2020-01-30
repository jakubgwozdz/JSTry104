package beveragebandits

import pathfinder.BasicPathfinder

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

}

infix fun Int.by(x: Int): Position = Position(this, x)

data class Cavern(internal val map: List<String>) {
    constructor(input: String) : this(input.lines())

    override fun toString(): String = map.joinToString("\n")

    fun canGoTo(position: Position) = this[position] == '.'

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


fun Cavern.killUnit(position: Position): Cavern {
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

fun Cavern.actionOrder() = map.indices
    .flatMap { y ->
        map[y].indices
            .filter { x -> map[y][x] in MobType.chars }
            .map { x -> y by x to MobType.of(map[y][x]) }
    }

enum class MobType(val char: Char) {
    Elf('E'), Goblin('G');

    companion object {
        val chars by lazy { values().map { it.char } }
        fun of(char: Char) = values().single { it.char == char }
    }
}

data class Mob(val position: Position, val type: MobType, val hp: Int = 200)

data class FightState(
    val cavern: Cavern,
    val elves: List<Mob>, val goblins: List<Mob>,
    val turnsCompleted: Int,
    val mobsToGo: List<Mob>
)

fun newFight(cavern: Cavern): FightState {
    val order = cavern.actionOrder().map { Mob(it.first, it.second) }
    val (elves, goblins) = order.partition { it.type == MobType.Elf }
    return FightState(cavern, elves, goblins, 0, order)
}

fun Mob.canAttack(cavern: Cavern): List<Direction> {
    val enemyType = when (type) {
        MobType.Elf -> MobType.Goblin
        MobType.Goblin -> MobType.Elf
    }
    return Direction.values()
        .filter { cavern[position + it] == enemyType.char }
}

fun FightState.fullRound(): FightState {
    var s = this
    while (s.mobsToGo.isNotEmpty()) s = s.mobTurn(s.mobsToGo.first())
    return s.copy(
        turnsCompleted = s.turnsCompleted + 1,
        mobsToGo = (s.elves + s.goblins).sortedBy { it.position })
}

fun FightState.mobTurn(mob: Mob): FightState {
    return this
        .run { if (mob.canAttack(cavern).isEmpty()) mobMove(mob) else this }
        .run { if (mob.canAttack(cavern).isNotEmpty()) mobAttack(mob) else this }
        .copy(mobsToGo = mobsToGo.drop(1))
}

fun FightState.chooseDestination(mob: Mob): Position? {
    val enemies = when (mob.type) {
        MobType.Elf -> goblins
        MobType.Goblin -> elves
    }

    if (enemies.isEmpty()) throw Exception("End of combat")

    val inRange = enemies
        .flatMap { e -> Direction.values().map { e.position + it } } // adjacent places
        .filter { cavern.canGoTo(it) } // that are empty

//    if (inRange.isEmpty()) return null

    val reachable = BasicPathfinder<Position>(
        waysOutOp = { list ->
            Direction.values().map { list.last() + it }
                .filter { cavern.canGoTo(it) }
                .filter { it !in list }
        },
        distanceOp = { list -> list.size * 10000 + list.last().y * 100 + list.last().x }
    ).findShortest(listOf(mob.position)) { list -> list.last() in inRange }

    return reachable?.last()
}

fun FightState.mobMove(mob: Mob): FightState {
    val destination = chooseDestination(mob)

    return if (destination != null) {
        val path = BasicPathfinder<Position>(
            waysOutOp = { list ->
                Direction.values().map { list.last() + it }.filter { cavern.canGoTo(it) }
            },
            distanceOp = { list -> list.size * 10000 + list.last().y * 100 + list.last().x }
        ).findShortest(listOf(mob.position)) { list -> list.last() == destination }!!
        val nextStep = path[1]
        val newMobState = mob.copy(position = nextStep)
        this.copy(
            cavern = cavern.mobMove(mob.position, nextStep),
            elves = elves.replace(mob, newMobState),
            goblins = goblins.replace(mob, newMobState),
            mobsToGo = mobsToGo.replace(mob, newMobState)
        )
    } else {
        this
    }
}

private fun <E> List<E>.replace(prev: E, now: E): List<E> {
    val i = this.indexOf(prev)
    return if (i < 0) this
    else this.subList(0, i) + now + this.subList(i + 1, this.size)
}

fun FightState.mobAttack(mob: Mob): FightState {
    // TODO()
    return this
}
