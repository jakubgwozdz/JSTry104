package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

internal class CavernTest {

    @Test
    fun canGoTo() {
        val cavern = Cavern(
            """
                #######
                #.G.E.#
                #E.G.E#
                #.G.E.#
                #######
                """.trimIndent()
        )
        expect(true) { cavern.canGoTo(1 by 1) }
        expect(false) { cavern.canGoTo(1 by 2) }
    }

    @Test
    fun actonOrder() {
        val cavern = Cavern(
            """
                #######
                #.G.E.#
                #E.G.E#
                #.G.E.#
                #######
                """.trimIndent()
        )

        expect(
            listOf(
                1 by 2 to MobType.Goblin, 1 by 4 to MobType.Elf,
                2 by 1 to MobType.Elf, 2 by 3 to MobType.Goblin, 2 by 5 to MobType.Elf,
                3 by 2 to MobType.Goblin, 3 by 4 to MobType.Elf
            )
        ) { cavern.actionOrder() }
    }

    @Test
    fun moveUnit() {
        val input = """
                #######
                #.G.E.#
                #E.G.E#
                #.G.E.#
                #######
                """.trimIndent()

        val output = """
                #######
                #G..E.#
                #E.GEE#
                #.G...#
                #######
                """.trimIndent()

        val cavern = Cavern(input)

        expect(output) {
            cavern.mobMove(1 by 2, Direction.W).mobMove(3 by 4, Direction.N).toString()
        }
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

        expect(1 by 3) { newFight(Cavern(input)).run { chooseDestination(this.mobsToGo.first()) } }

    }

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
            newFight(Cavern(input))
                .run { mobMove(this.mobsToGo.first()) }
                .cavern.toString()
        }

    }

    @Test
    fun moveMobB() {
        val input = """
            #######
            #.E...#
            #.....#
            #...G.#
            #######
            """.trimIndent()
        val output = """
            #######
            #..E..#
            #.....#
            #...G.#
            #######
            """.trimIndent()

        val state0 = newFight(Cavern(input))
        expect(1 by 2) { state0.elves.single().position }
        expect(3 by 4) { state0.goblins.single().position }
        expect(1 by 2) { state0.mobsToGo.first().position }

        val state1 = state0.run { mobMove(this.mobsToGo.first()) }
        expect(1 by 3) { state1.elves.single().position }
        expect(3 by 4) { state1.goblins.single().position }
        expect(1 by 3) { state1.mobsToGo.first().position }

        expect(output) { state1.cavern.toString() }

    }

    @Test
    fun mobTurnB() {
        val input = """
            #######
            #.E...#
            #.....#
            #...G.#
            #######
            """.trimIndent()
        val output = """
            #######
            #..E..#
            #.....#
            #...G.#
            #######
            """.trimIndent()

        val state0 = newFight(Cavern(input))
        expect(1 by 2) { state0.elves.single().position }
        expect(3 by 4) { state0.goblins.single().position }
        expect(1 by 2) { state0.mobsToGo.first().position }

        val state1 = state0.run { mobTurn(this.mobsToGo.first()) }
        expect(1 by 3) { state1.elves.single().position }
        expect(3 by 4) { state1.goblins.single().position }
        expect(3 by 4) { state1.mobsToGo.first().position }

        expect(output) { state1.cavern.toString() }

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

        val state0 = newFight(Cavern(input))
        val state1 = state0.fullRound()
        expect(1) { state1.turnsCompleted }
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
        ) { state1.cavern.toString() }
        val state3 = state1.fullRound().fullRound()
        val state4 = state3.fullRound()

        expect(output) { state3.cavern.toString() }
        expect(output) { state4.cavern.toString() }

    }

}
