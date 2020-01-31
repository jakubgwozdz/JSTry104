package beveragebandits

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

sealed class FightState(
    open val cavern: Cavern,
    open val mobs: List<Mob>,
    open val roundsCompleted: Int
) {

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

sealed class FightInProgress(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int) : FightState(cavern, mobs, roundsCompleted)

sealed class MobTurn(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int,
    open val next: Int
    ) : FightInProgress(cavern, mobs, roundsCompleted)


data class MovePhase(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int,
    override val next: Int
) : MobTurn(cavern, mobs, roundsCompleted, next)

data class AttackPhase(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int,
    override val next: Int
) : MobTurn(cavern, mobs, roundsCompleted, next)

data class EndPhase(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int,
    override val next: Int
) : MobTurn(cavern, mobs, roundsCompleted, next)

data class EndOfRound(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int
) : FightInProgress(cavern, mobs, roundsCompleted)


data class FightEnded(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val roundsCompleted: Int
) : FightState(cavern, mobs, roundsCompleted)


