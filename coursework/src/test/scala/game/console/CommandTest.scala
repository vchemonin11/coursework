package game.console

import game.{Move, Rewind, Exit, Invalid}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Created by chemonin on 09.11.2020.
 */
class CommandTest extends AsyncFunSuite with Matchers {

  test("commands parsed ok") {
    Determinant.getCommand("exit") shouldBe Exit
    Determinant.getCommand("m 0 1 2") shouldBe Move(0, 1, 2)
    Determinant.getCommand("r 5") shouldBe Rewind(5)
    Determinant.getCommand("1 2 3") shouldBe Invalid
  }
}
