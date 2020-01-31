package beveragebandits

import beveragebandits.MobType.Elf
import beveragebandits.MobType.Goblin
import pathfinder.BFSPathfinder
import pathfinder.Cache
import utils.replace

class FightRules {

    fun newFight(cavern: Cavern): MovePhase {
        val mobs = cavern.mobs().mapIndexed { id, (position, type) -> Mob(position, type) }
        return MovePhase(cavern, mobs.sortedBy { it.position }, 0, 0)
    }

    fun fightToEnd(state: FightInProgress): FightEnded {
        var s: FightState = state
        while (true) {
            s = when (s) {
                is MobTurn -> fullRound(s)
                is EndOfRound -> nextRound(s)
                is FightEnded -> return s
            }
        }
    }

    fun nextRound(state: EndOfRound): MovePhase {
        return MovePhase(
            cavern = state.cavern,
            mobs = state.mobs.sortedBy { it.position },
            roundsCompleted = state.roundsCompleted + 1,
            next = 0
        )
    }

    fun fullRound(state: MobTurn): FightState {

        var s: FightState = state
        while (s is MobTurn && s.next in s.mobs.indices) {
            s = when {
                s.mobs[s.next].hp <= 0 -> nextMob(s)
                s.mobs.none { it.type == Goblin && it.hp > 0 } -> FightEnded(s.cavern, s.mobs, s.roundsCompleted)
                s.mobs.none { it.type == Elf && it.hp > 0 } -> FightEnded(s.cavern, s.mobs, s.roundsCompleted)
                else -> mobTurn(s)
            }
        }

        return s
    }

    fun nextMob(state: MobTurn): FightState {
        return if (state.next + 1 < state.mobs.size)
            MovePhase(state.cavern, state.mobs, state.roundsCompleted, state.next + 1)
        else
            EndOfRound(state.cavern, state.mobs, state.roundsCompleted)
    }

    fun mobTurn(state: MobTurn): FightState {
        var s: FightState = state
        while (s is MobTurn && s.next==state.next) {
            s = mobPhase(s)
        }
        return s
    }

    private fun mobPhase(s: MobTurn): FightState {
        return when (s) {
            is MovePhase -> movePhase(s)
            is AttackPhase -> attackPhase(s)
            is EndPhase -> nextMob(s)
        }
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

    fun movePhase(fightState: MovePhase): AttackPhase {

        val mob = fightState.mobs[fightState.next]

        val enemyPositions = fightState.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val destination = chooseDestination(mob.position, enemyPositions, fightState.cavern)
            ?: return AttackPhase(fightState.cavern, fightState.mobs, fightState.roundsCompleted, fightState.next)

        val nextStep = nextStep(mob.position, destination, fightState.cavern)
        val newMobState = mob.copy(position = nextStep)
        return AttackPhase(
            cavern = fightState.cavern.mobMove(mob.position, nextStep),
            mobs = fightState.mobs.replace(mob, newMobState),
            roundsCompleted = fightState.roundsCompleted,
            next = fightState.next
        )
    }

    data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
        override fun compareTo(other: AttackPriority): Int =
            compareValuesBy(this, other, { it.hp }, { it.position })
    }

    fun attackPhase(fightState: AttackPhase): EndPhase {

        val mob = fightState.mobs[fightState.next]

        val enemy = fightState.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .filter { it.position.isAdjacentTo(mob.position) }
            .minBy { AttackPriority(it.hp, it.position) }
            ?: return EndPhase(fightState.cavern, fightState.mobs, fightState.roundsCompleted, fightState.next)

        val newEnemyState = enemy.copy(hp = enemy.hp - 3)

        return if (newEnemyState.hp > 0) {
            EndPhase(
                cavern = fightState.cavern,
                mobs = fightState.mobs.replace(enemy, newEnemyState),
                roundsCompleted = fightState.roundsCompleted,
                next = fightState.next
            )
        } else {
            EndPhase(
                cavern = fightState.cavern.without(enemy.position),
                mobs = fightState.mobs.replace(enemy, newEnemyState),
                roundsCompleted = fightState.roundsCompleted,
                next = fightState.next
            )
        }
    }
}
