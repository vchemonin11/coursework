package game.console

import game.{Move, Rewind}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Created by chemonin on 09.11.2020.
 */
class RewindTest extends AsyncFunSuite with Matchers {

  test("rewind correctly") {

    val game = Game.initGame(Player("s"), Player("v"), 3)
    val moves = Seq(Move(1, 0, 2), Move(2, 1, 4), Move(0, 0, 5), Move(2, 2, 6))
    val gameAfterMoves = moves.foldLeft(game)((g, m) => g.makeMove(m))
    gameAfterMoves.printGame()
    val afterRewind = gameAfterMoves.rewind(Rewind(2))
    val afterDiffMove = afterRewind.get.makeMove(Move(0, 0, 6))
    afterDiffMove.printGame()
    afterDiffMove.fields.length shouldBe 4
    assert(afterDiffMove.fields(afterDiffMove.fields.length - 1).arr(0).get == 6)
  }

}
