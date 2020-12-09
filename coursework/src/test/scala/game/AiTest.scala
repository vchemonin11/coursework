package game

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Created by chemonin on 09.11.2020.
 */
class AiTest extends AsyncFunSuite with Matchers {

  test("Ai move fast") {
    val size = 3
    val emptyVec = Vector.fill(size * size)(None)
    val initField = Field(emptyVec.updated(0, Some(5)).updated(1, Some(9)), size)
    val ai = AI(size)
    println("Begin")
    val t0 = System.nanoTime()
    val res = ai.solve_matrix_flat(initField)
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1e9d + "s")
    println(res)
    res._1 shouldBe 40
    res._2 shouldBe 3
    res._3 shouldBe 6

    val newField = initField.makeMove(ai.moveFromRes(res))
    newField.printField()
    newField.moveCount shouldBe 3
  }

  test("Ai slow test") {
    val size = 3
    val emptyVec = Vector.fill(size * size)(None)
    val initField = Field(emptyVec.updated(0, Some(5)), size)
    val ai = AI(size)
    println("Begin")
    val t0 = System.nanoTime()
    val res = ai.solve_matrix_flat(initField)
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1e9d + "s")
    println(res)
    res._1 shouldBe 40
    res._2 shouldBe 8
    res._3 shouldBe 1

    val newField = initField.makeMove(ai.moveFromRes(res))
    newField.printField()
    newField.moveCount shouldBe 2
  }

  test("Ai very slow test") {
    val size = 3
    val emptyVec = Vector.fill(size * size)(None)
    val initField = Field(emptyVec, size)
    val ai = AI(size)
    println("Begin")
    val t0 = System.nanoTime()
    val res = ai.solve_matrix_flat(initField)
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) / 1e9d + "s")
    println(res)
    res._1 shouldBe 40
    res._2 shouldBe 4
    res._3 shouldBe 0

    val newField = initField.makeMove(ai.moveFromRes(res))
    newField.printField()
    newField.moveCount shouldBe 1
  }

  test("Ai last move") {
    val size = 3
    val initVec = Vector(Some(1), Some(9), None,
      None, Some(5), Some(4),
      Some(3), Some(6), Some(2))
    val initField = Field(initVec, size)
    val ai = AI(size)
    val newState = initField.makeMove(Move(2, 0, 7))
    val move = ai.next(newState)
    newState.moveCount shouldBe 8
    move.value shouldBe 8
  }

}
