package beveragebandits

data class AttackPriority(val hp: Int, val position: Position) : Comparable<AttackPriority> {
    override fun compareTo(other: AttackPriority): Int =
        compareValuesBy(this, other, { it.hp }, { it.position })
}

fun chooseEnemy(mob: Mob, allMobs: List<Mob>): Mob? {
    return allMobs
        .filterNot { it.type == mob.type }
        .filter { it.hp > 0 }
        .filter { it.position.isAdjacentTo(mob.position) }
        .minBy { AttackPriority(it.hp, it.position) }
}
