package game

import game.db.MoveRecord
import game.service.Helper
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Created by chemonin on 09.11.2020.
 */
class FieldTest extends AsyncFunSuite with Matchers {

  test("printing correctly") {
    val arr = Vector(Some(1), None, None, None, Some(2), None, None, None, None)
    val f = Field(arr, 3)
    f.printField()
    assert(!f.isComlete)
  }

  test("empty field fold") {
    val moves: Seq[MoveRecord] = Nil
    val f = Field.emptyField(3)
    val newField = moves.foldLeft(f)((f, m) => f.makeMove(Helper.moveFromRecord(m)))
    newField.moveCount shouldBe 0
  }

}
