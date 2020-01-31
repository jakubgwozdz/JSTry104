package beveragebandits

import kotlin.test.Test
import kotlin.test.expect

class MoveTest {

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
                .run {
                    mobMove(
                        this.mobsToGo.first(), when (mobsToGo.first().type) {
                            MobType.Elf -> goblins
                            MobType.Goblin -> elves
                        }
                    ).first
                }
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

        val state1 = state0.run {
            mobMove(
                this.mobsToGo.first(), when (mobsToGo.first().type) {
                    MobType.Elf -> goblins
                    MobType.Goblin -> elves
                }
            ).first
        }
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

        val state1 = state0.run {
            mobTurn(this.mobsToGo.first(), goblins)
        }
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
        val state1 = state0.fullRound() as FightInProgress
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
        val state3 = (state1.fullRound() as FightInProgress).fullRound() as FightInProgress
        val state4 = state3.fullRound()

        expect(output) { state3.cavern.toString() }
        expect(output) { state4.cavern.toString() }
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
            newFight(Cavern(input)).run {
                chooseDestination(
                    this.mobsToGo.first(), when (mobsToGo.first().type) {
                        MobType.Elf -> goblins
                        MobType.Goblin -> elves
                    }
                )
            }
        }
    }
}