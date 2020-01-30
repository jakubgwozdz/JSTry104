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


}
