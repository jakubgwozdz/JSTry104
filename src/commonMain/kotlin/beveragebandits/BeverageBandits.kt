package beveragebandits

import pathfinder.BFSPathfinder
import pathfinder.BasicPathfinder
import pathfinder.Cache
import utils.replace
import utils.without

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
) {
    override fun toString(): String = "After $turnsCompleted rounds: \n$cavern"
}

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
    val initial = this
    val (afterMove, mobAfterMove) = if (mob.canAttack(initial.cavern).isEmpty()) initial.mobMove(mob) else initial to mob
    val afterAttack = if (mobAfterMove.canAttack(afterMove.cavern).isNotEmpty()) afterMove.mobAttack(mobAfterMove) else afterMove

    return afterAttack.copy(mobsToGo = mobsToGo.drop(1))
}

data class MovePriority(val distance: Int, val position: Position) : Comparable<MovePriority> {
    override fun compareTo(other: MovePriority): Int = compareValuesBy(this, other, { it.distance }, { it.position })
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

    if (inRange.isEmpty()) return null

    val cache = Cache<Position, MovePriority>()

    val reachable = BFSPathfinder<Position, List<Position>, MovePriority>(
        // logWithTime = { println(it()) },
        waysOutOp = { l ->
            Direction.values().map { l.last() + it }
                .filter { cavern.canGoTo(it) }
                .filter { it !in l }
        },
        distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else l.single()) },
        adderOp = { l, t -> l + t },
        meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
    ).findShortest(listOf(mob.position)) { list -> list.last() in inRange }

    return reachable?.last()
}

fun FightState.mobMove(mob: Mob): Pair<FightState, Mob> {
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
        ) to newMobState
    } else {
        this to mob
    }
}

data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
    override fun compareTo(other: AttackPriority): Int = compareValuesBy(this, other, { it.hp }, { it.position })
}

fun FightState.mobAttack(mob: Mob): FightState {
    val enemy = when (mob.type) {
        MobType.Elf -> goblins
        MobType.Goblin -> elves
    }
        .filter { it.position.isAdjacentTo(mob.position) }
        .minBy { AttackPriority(it.hp, it.position) }!!
    val newEnemyState = enemy.copy(hp = enemy.hp - 3)

    return if (newEnemyState.hp > 0) {
        this.copy(
            elves = elves.replace(enemy, newEnemyState),
            goblins = goblins.replace(enemy, newEnemyState),
            mobsToGo = mobsToGo.replace(enemy, newEnemyState)
        )
    } else {
        this.copy(
            cavern = cavern.killUnit(enemy.position),
            elves = elves.without(enemy),
            goblins = goblins.without(enemy),
            mobsToGo = mobsToGo.without(enemy)
        )
    }
}

