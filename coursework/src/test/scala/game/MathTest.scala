package game

import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

/**
 * Created by chemonin on 05.12.2020.
 */
class MathTest extends AsyncFunSuite with Matchers {

  test("determinant 3 count correct") {
    val size = 3;
    val arr0 = Vector(1, 2, 3, 4, 5, 6, 7, 8, 9)
    Math.determinant(arr0, size)  shouldBe 0

    val arr40 = Vector(1, 9, 6, 8, 5, 4, 7, 3, 2)
    Math.determinant(arr40, size)  shouldBe 40

  }

  test("determinant 2 correct") {
    val size = 2
    val arrM2 = Vector(1, 2, 3, 4)
    Math.determinant(arrM2, size)  shouldBe -2

    val arr2 = Vector(2, 1, 4, 3)
    Math.determinant(arr2, size)  shouldBe 2
  }

}
