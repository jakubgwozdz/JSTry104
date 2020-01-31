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
            fightRules.newFight(Cavern(input))
                .let { fightRules.movePhase(it) }
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

        val state0 = fightRules.newFight(Cavern(input))
        val state1 = fightRules.fullRound(state0).let { fightRules.nextRound(it as EndOfRound) }
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
                .let { fightRules.fullRound(it) }
                .let { fightRules.nextRound(it as EndOfRound) }
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
            val fight = fightRules.newFight(Cavern(input))
            val first = fight.state.mobs[fight.mobIndex]
            fightRules.chooseDestination(
                first.position,
                fight.state.mobs.filterNot { it.type == first.type }.map { it.position },
                fight.state.cavern
            )
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

        val fight = fightRules.newFight(Cavern(input))
        val first = fight.state.mobs[fight.mobIndex]
        val destination = fightRules.chooseDestination(
            first.position,
            fight.state.mobs.filterNot { it.type == first.type }.map { it.position },
            fight.state.cavern
        )!!

        expect(8 by 8) { destination }

        expect(1 by 4) { fightRules.nextStep(first.position, destination, fight.state.cavern) }
    }
}