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
    open val turnsCompleted: Int
){
    override fun toString(): String {

        val state = cavern.map.mapIndexed { y, l ->
            val mobsOnLine = mobs.filter { it.position.y == y }
                .sortedBy { it.position }
            "$l   ${mobsOnLine.joinToString(", ") { "${it.type.char}(${it.hp})" }}".trim()
        }.joinToString("\n")

        return "After $turnsCompleted rounds:\n$state"
    }

    val outcome get() = turnsCompleted * mobs.sumBy { it.hp }

}

data class FightInProgress(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val turnsCompleted: Int,
    val toGo: List<Mob>
) : FightState(cavern, mobs, turnsCompleted) {
    override fun toString(): String = super.toString()
}

data class FightEnded(
    override val cavern: Cavern,
    override val mobs: List<Mob>,
    override val turnsCompleted: Int
) : FightState(cavern, mobs, turnsCompleted) {

    override fun toString(): String = super.toString()
}

