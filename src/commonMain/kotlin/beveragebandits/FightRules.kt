package beveragebandits

import pathfinder.BFSPathfinder
import pathfinder.Cache
import utils.replace

class FightRules {

    fun newFight(cavern: Cavern): FightInProgress {
        val mobs = cavern.mobs().mapIndexed { id, (position, type) -> Mob(position, type) }
        return FightInProgress(cavern, mobs.sortedBy { it.position }, 0, mobs.indices.first)
    }

    private fun nextTurn(fightState: FightInProgress): FightInProgress {
        return fightState.copy(
            mobs = fightState.mobs.sortedBy { it.position },
            turnsCompleted = fightState.turnsCompleted + 1,
            next = fightState.mobs.indices.first
        )
    }

    fun fightToEnd(fightState: FightInProgress): FightEnded {
        var s: FightState = fightState
        while (s is FightInProgress) {
            s = fullRound(s)
        }
        return s as FightEnded
    }

    fun fullRound(fightState: FightInProgress): FightState {

        var s = fightState
        while (s.next in s.mobs.indices) {

            if (s.mobs[s.next].hp <= 0) continue

            if (s.mobs.none { it.type == MobType.Goblin && it.hp > 0 } || s.mobs.none { it.type == MobType.Elf && it.hp > 0 }) {
                return FightEnded(s.cavern, s.mobs, s.turnsCompleted)
            }

            val s2 = nextMobTurn(s)
            s = s2
        }

        return nextTurn(s)
    }

    fun nextMobTurn(fightState: FightInProgress): FightInProgress {

        return fightState.let { movePhase(it) }
            .let { attackPhase(it) }
            .let { it.copy(next = it.next + 1) }
    }

    data class MovePriority(val distance: Int, val position: Position?) : Comparable<MovePriority> {
        override fun compareTo(other: MovePriority): Int =
            compareValuesBy(this, other, { it.distance }, { it.position })
    }

    fun chooseDestination(
        from: Position,
        enemyPositions: Collection<Position>,
        cavern: Cavern
    ): Position? {

        if (enemyPositions.any { it.isAdjacentTo(from) }) return null

        val inRange = enemyPositions
            .flatMap { e -> Direction.values().map { e + it } } // adjacent places
            .filter { cavern.emptyAt(it) } // that are empty

        if (inRange.isEmpty()) return null

        val cache =
            Cache<Position, MovePriority>()

        val reachable = BFSPathfinder<Position, List<Position>, MovePriority>(
            // logWithTime = { println(it()) },
            adderOp = { l, t -> l + t },
            waysOutOp = { l ->
                Direction.values().map { l.last() + it }
                    .filter { cavern.emptyAt(it) }
                    .filter { it !in l }
            },
            distanceOp = { l -> MovePriority(l.size, l.last()) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(from)) { list -> list.last() in inRange }

        return reachable?.last()
    }

    fun nextStep(
        from: Position,
        destination: Position,
        cavern: Cavern
    ): Position {
        val cache = Cache<Position, MovePriority>()

        val path = BFSPathfinder<Position, List<Position>, MovePriority>(
            adderOp = { l, t -> l + t },
            waysOutOp = { list ->
                Direction.values().map { list.last() + it }
                    .filter { cavern.emptyAt(it) }
            },
            distanceOp = { l -> MovePriority(l.size, if (l.size > 1) l[1] else null) },
            meaningfulOp = { l, d -> cache.isBetterThanPrevious(l.last(), d) }
        ).findShortest(listOf(from)) { list -> list.last() == destination }!!
        return path[1]
    }

    fun movePhase(fightState: FightInProgress): FightInProgress {

        val mob = fightState.mobs[fightState.next]

        if (mob.hp <= 0) return fightState

        val enemyPositions = fightState.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val destination = chooseDestination(mob.position, enemyPositions, fightState.cavern)
            ?: return fightState

        val nextStep = nextStep(mob.position, destination, fightState.cavern)
        val newMobState = mob.copy(position = nextStep)
        return fightState.copy(
            cavern = fightState.cavern.mobMove(mob.position, nextStep),
            mobs = fightState.mobs.replace(mob, newMobState)
        )
    }

    data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
        override fun compareTo(other: AttackPriority): Int =
            compareValuesBy(this, other, { it.hp }, { it.position })
    }

    fun attackPhase(fightState: FightInProgress): FightInProgress {

        val mob = fightState.mobs[fightState.next]
        if (mob.hp <= 0) return fightState
        val enemy = fightState.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .filter { it.position.isAdjacentTo(mob.position) }
            .minBy { AttackPriority(it.hp, it.position) }
            ?: return fightState

        val newEnemyState = enemy.copy(hp = enemy.hp - 3)

        return if (newEnemyState.hp > 0) {
            fightState.copy(
                mobs = fightState.mobs.replace(enemy, newEnemyState)
            )
        } else {
            fightState.copy(
                cavern = fightState.cavern.without(enemy.position),
                mobs = fightState.mobs.replace(enemy, newEnemyState)
            )
        }
    }
}
