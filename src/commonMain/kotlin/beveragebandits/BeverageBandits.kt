package beveragebandits

import utils.replace

enum class MobType(val char: Char) {
    Elf('E'), Goblin('G');

    companion object {
        val chars by lazy { values().map { it.char } }
        fun of(char: Char) = values().single { it.char == char }
    }
}

data class Mob(val position: Position, val type: MobType, val hp: Int = 200) {
    fun isAdjacentTo(other: Mob): Boolean = position.isAdjacentTo(other.position)
}

data class CombatState(
    val cavern: Cavern,
    val mobs: List<Mob>,
    val roundsCompleted: Int
) {

    constructor(cavern: Cavern) : this(
        cavern,
        cavern.mobs().map { (position, type) -> Mob(position, type) }.sortedBy { it.position },
        0
    )

    fun nextRound() = CombatState(cavern, mobs.sortedBy { it.position }, roundsCompleted + 1)

    fun moveMob(mob: Mob, destination: Position) = CombatState(
        cavern.mobMove(mob.position, destination),
        mobs.replace(mob, mob.copy(position = destination)),
        roundsCompleted
    )

    fun hitMob(mob: Mob, attackPower: Int):CombatState {
        val newHp = mob.hp - attackPower
        val c = if (newHp <= 0) cavern.without(mob.position) else cavern
        return CombatState(c, mobs.replace(mob, mob.copy(hp = newHp)), roundsCompleted)
    }

    fun description(): String {

        val state = cavern.map.mapIndexed { y, l ->
            val mobsOnLine = mobs.filter { it.position.y == y && it.hp > 0 }
                .sortedBy { it.position }
            "$l   ${mobsOnLine.joinToString(", ") { "${it.type.char}(${it.hp})" }}".trim()
        }.joinToString("\n")

        return "After $roundsCompleted rounds:\n$state"
    }

    val outcome get() = roundsCompleted * mobs.filter { it.hp > 0 }.sumBy { it.hp }
}

sealed class Phase(
    open val state: CombatState
)

sealed class CombatInProgress(override val state: CombatState) : Phase(state)

sealed class MobTurn(
    state: CombatState,
    open val mobIndex: Int
) : CombatInProgress(state)

data class Move(
    override val state: CombatState,
    override val mobIndex: Int
) : MobTurn(state, mobIndex)

data class Attack(
    override val state: CombatState,
    override val mobIndex: Int
) : MobTurn(state, mobIndex)

data class EndTurn(
    override val state: CombatState,
    override val mobIndex: Int
) : MobTurn(state, mobIndex)

data class EndOfRound(
    override val state: CombatState
) : CombatInProgress(state)

sealed class CombatEnded(
    state: CombatState
) : Phase(state)

data class ElvesWins(
    override val state: CombatState
) : CombatEnded(state)

data class GoblinsWins(
    override val state: CombatState
) : CombatEnded(state)

