package beveragebandits

import utils.replace

enum class MobType(val char: Char) {
    Elf('E'), Goblin('G');

    companion object {
        val chars by lazy { values().map { it.char } }
        fun of(char: Char) = values().single { it.char == char }
    }
}

data class Mob(val id:Int, val position: Position, val type: MobType, val hp: Int = 200) {
    fun withPosition(p: Position) = copy(position = p)
    fun withHp(hp: Int) = copy(hp = hp)
}

data class CombatState(
    val cavern: Cavern,
    val mobs: List<Mob>,
    val roundsCompleted: Int
) {

    constructor(cavern: Cavern) : this(
        cavern,
        cavern.mobs().mapIndexed { id, (position, type) -> Mob(id, position, type) }.sortedBy { it.position },
        0
    )

    fun nextRound() = CombatState(cavern, mobs.sortedBy { it.position }, roundsCompleted + 1)

    fun moveMob(mob: Mob, destination: Position) = CombatState(
        cavern.mobMove(mob.position, destination),
        mobs.replace(mob, mob.withPosition(destination)),
        roundsCompleted
    )
        .also { it.consistencyCheck() }

    fun hitMob(mob: Mob, attackPower: Int): CombatState {
        val newHp = mob.hp - attackPower
        val c = if (newHp <= 0) cavern.without(mob.position) else cavern
        return CombatState(c, mobs.replace(mob, mob.withHp(newHp)), roundsCompleted)
            .also { it.consistencyCheck() }
    }

    fun description(): String {

        val state = cavern.map.mapIndexed { y, l ->
            val mobsOnLine = mobs.filter { it.position.y == y && it.hp > 0 }
                .sortedBy { it.position }
            "$l   ${mobsOnLine.joinToString(", ") { "${it.type.char}(${it.hp})" }}".trim()
        }.joinToString("\n")

        return "After $roundsCompleted rounds:\n$state"
    }

    internal fun consistencyCheck() =
        check(cavern.mobs().map { it.first }.sorted() == mobs.filter { it.hp > 0 }.map { it.position }.sorted()) {
            "inconsistency in state"
        }

    val outcome get() = roundsCompleted * mobs.filter { it.hp > 0 }.sumBy { it.hp }
}

sealed class Phase(
    val state: CombatState
)

sealed class CombatInProgress(
    state: CombatState
) : Phase(state)

class StartOfCombat(
    state: CombatState
): CombatInProgress(state)

class StartOfRound(
    state: CombatState
) : CombatInProgress(state)

sealed class MobTurn(
    state: CombatState,
    val mobIndex: Int
) : CombatInProgress(state)

class StartOfTurn(
    state: CombatState,
    mobIndex: Int
) : MobTurn(state, mobIndex)

class Move(
    state: CombatState,
    mobIndex: Int
) : MobTurn(state, mobIndex)

class Attack(
    state: CombatState,
    mobIndex: Int
) : MobTurn(state, mobIndex)

class EndOfTurn(
    state: CombatState,
    mobIndex: Int
) : MobTurn(state, mobIndex)

class EndOfRound(
    state: CombatState
) : CombatInProgress(state)

sealed class EndOfCombat(
    state: CombatState
) : Phase(state)

class ElvesWin(
    state: CombatState
) : EndOfCombat(state)

class GoblinsWin(
    state: CombatState
) : EndOfCombat(state)

