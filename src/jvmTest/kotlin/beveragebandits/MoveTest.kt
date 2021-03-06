package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

class MoveTest {

    private val fightRules = FightRules()

    @Test
    fun moveMobA() {
        val input = """
            #######
            #E..G.#
            #...#.#
            #.G.#G#
            #######
            """.trimIndent()
        val output = """
            #######
            #.E.G.#
            #...#.#
            #.G.#G#
            #######
            """.trimIndent()

        expect(output) {
            fightRules.newCombat(Cavern(input))
                .let { fightRules.firstRound(it) }
                .let { fightRules.firstMob(it) }
                .let { fightRules.beginTurn(it) }
                .let { fightRules.movePhase(it as Move) }
                .state.cavern.toString()
        }
    }

    @Test
    fun fullRound3() {
        val input = """
            #########
            #G..G..G#
            #.......#
            #.......#
            #G..E..G#
            #.......#
            #.......#
            #G..G..G#
            #########
            """.trimIndent()
        val output = """
            #########
            #.......#
            #..GGG..#
            #..GEG..#
            #G..G...#
            #......G#
            #.......#
            #.......#
            #########
            """.trimIndent()

        val state0 = fightRules.newCombat(Cavern(input))
            .let { fightRules.firstRound(it) }
            .let { fightRules.firstMob(it) }
        // .let { fightRules.beginTurn(it) }

        val state1 = fightRules.fullRound(state0)
            .let { fightRules.nextRound(it as EndOfRound) }
            .let { fightRules.firstMob(it) }
            .let { fightRules.beginTurn(it) }

        expect(1) { state1.state.roundsCompleted }
        expect(
            """
    #########
    #.G...G.#
    #...G...#
    #...E..G#
    #.G.....#
    #.......#
    #G..G..G#
    #.......#
    #########
    """.trimIndent()
        ) { state1.state.cavern.toString() }
        val state3 =
            fightRules.fullRound(state1)
                .let { fightRules.nextRound(it as EndOfRound) }
                .let { fightRules.firstMob(it) }
                .let { fightRules.beginTurn(it) }
                .let { fightRules.fullRound(it) }
                .let { fightRules.nextRound(it as EndOfRound) }
                .let { fightRules.firstMob(it) }
                .let { fightRules.beginTurn(it) }

        val state4 = fightRules.fullRound(state3)

        expect(output) { state3.state.cavern.toString() }
        expect(output) { state4.state.cavern.toString() }
    }

    @Test
    fun chooseDestination() {
        val input = """
            #######
            #E..G.#
            #...#.#
            #.G.#G#
            #######
            """.trimIndent()

        expect(1 by 3) {
            val fight = fightRules.newCombat(Cavern(input))
                .let { fightRules.firstRound(it) }
                .let { fightRules.firstMob(it) }
                .let { fightRules.beginTurn(it) }

            val first = fight.state.mobs[fight.mobIndex]
            chooseDestination(
                first.position,
                fight.state.mobs.filterNot { it.type == first.type }.map { it.position },
                fight.state.cavern
            )?.last()
        }
    }

    @Test
    fun nextStep() {
        val input = """
            ##########
            #..G.....#
            #.#....#.#
            #....#.#.#
            #..###...#
            ##...##..#
            ###.....##
            #####..#E#
            #####....#
            ##########
            """.trimIndent()

        val fight = fightRules.newCombat(Cavern(input))
            .let { fightRules.firstRound(it) }
            .let { fightRules.firstMob(it) }
            .let { fightRules.beginTurn(it) }

        val first = fight.state.mobs[fight.mobIndex]
        val path = chooseDestination(
            first.position,
            fight.state.mobs.filterNot { it.type == first.type }.map { it.position },
            fight.state.cavern
        )!!

        expect(8 by 8) { path.last() }

        expect(1 by 4) { path[1] }
    }
}
