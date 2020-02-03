package beveragebandits

import beveragebandits.MobType.Elf
import beveragebandits.MobType.Goblin

class FightRules(
    val elvesAttackPower: Int = 3,
    val goblinsWin: (CombatState) -> Boolean = { s -> s.mobs.none { it.type == Elf && it.hp > 0 } }
) {

    val elvesWin: (CombatState) -> Boolean = { s -> s.mobs.none { it.type == Goblin && it.hp > 0 } }
    val goblinsAttackPower = 3

    // actual rules:

    fun movePhase(p: Move): Phase {

        if (goblinsWin(p.state)) return GoblinsWin(p.state)
        if (elvesWin(p.state)) return ElvesWin(p.state)

        val mob = p.state.mobs[p.mobIndex]

        val enemyPositions = p.state.mobs
            .filterNot { it.type == mob.type }
            .filter { it.hp > 0 }
            .map { it.position }

        val path = chooseDestination(mob.position, enemyPositions, p.state.cavern)

        val newState = path
            ?.let { ns -> p.state.moveMob(mob, ns[1]) }
            ?: p.state

        return Attack(newState, p.mobIndex)
            .also { if (path != null) reporting.mobMoves(mob, path) }
    }

    fun attackPhase(p: Attack): EndOfTurn {

        val mob = p.state.mobs[p.mobIndex]
        val enemy = chooseEnemy(mob, p.state.mobs)
        val attackPower = if (mob.type == Elf) elvesAttackPower else goblinsAttackPower

        val newState = enemy
            ?.let { p.state.hitMob(it, attackPower) }
            ?: p.state

        return EndOfTurn(newState, p.mobIndex)
            .also {
                if (enemy != null)
                    reporting.mobAttacks(mob, enemy, attackPower)
            }
    }

    fun newCombat(cavern: Cavern): StartOfCombat = StartOfCombat(CombatState(cavern))
        .also { reporting.combatPhase(it) }

    fun singlePhase(phase: CombatInProgress): Phase = when (phase) {
        is StartOfCombat -> firstRound(phase) // => StartOfRound(round = 0)
        is StartOfRound -> firstMob(phase) // => StartOfTurn(mobIndex = 0)
        is StartOfTurn -> beginTurn(phase) // if (alive) => Move else => EndOfTurn
        is Move -> movePhase(phase)        // if (hasEnemies) => Attack else => CombatEnded
        is Attack -> attackPhase(phase)    // => EndOfTurn
        is EndOfTurn -> nextMob(phase)     // if (allMobsPlayed) => EndOfRound else => StartOfTurn(mobIndex++)
        is EndOfRound -> nextRound(phase)  // => StartOfRound(round++)
    }
        .also { reporting.combatPhase(it) }

    fun firstRound(s: StartOfCombat) = StartOfRound(s.state.also { check(it.roundsCompleted == 0) })

    fun firstMob(s: StartOfRound) = StartOfTurn(s.state, 0)

    fun nextMob(phase: EndOfTurn): Phase =
        if (phase.mobIndex + 1 in phase.state.mobs.indices)
            StartOfTurn(phase.state, phase.mobIndex + 1)
        else
            EndOfRound(phase.state)

    fun nextRound(phase: EndOfRound) = StartOfRound(phase.state.nextRound())

    fun beginTurn(p: StartOfTurn) =
        if (p.state.mobs[p.mobIndex].hp > 0) Move(p.state, p.mobIndex)
        else EndOfTurn(p.state, p.mobIndex)

    // additional loop methods

    fun fightToEnd(phase: CombatInProgress): EndOfCombat {
        var p: Phase = phase
        while (true) {
            p = when (p) {
                is EndOfCombat -> return p
                is CombatInProgress -> singlePhase(p)
            }
        }
    }

    fun fullRound(phase: MobTurn): Phase {
        var p: Phase = phase
        while (true) {
            p = when (p) {
                is EndOfCombat -> return p
                is EndOfRound -> return p
                is CombatInProgress -> singlePhase(p) // else
            }
        }
    }

    fun mobTurn(phase: MobTurn): Phase {
        var p: Phase = phase
        while (true) {
            p = when (p) {
                is EndOfCombat -> return p
                is EndOfRound -> return p
                is EndOfTurn -> return p
                is CombatInProgress -> singlePhase(p) // else
            }
        }
    }
}
