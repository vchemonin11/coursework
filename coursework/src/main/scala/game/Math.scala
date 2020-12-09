package game

/**
 * Created by chemonin on 09.11.2020.
 */
object Math {

  // type CollectionWithIndex[A]  = { def apply(idx: Int ): A }

  def determinant3[T: Numeric](a: IndexedSeq[T])(implicit n: Numeric[T]): T = {
    import n._
    a(0) * a(4) * a(8) + a(6) * a(1) * a(5) + a(3) * a(7) * a(2) - a(2) * a(4) * a(6) - a(1) * a(3) * a(8) - a(0) * a(5) * a(7)
  }

  // optimized fast version
  def determinant3Array(a: Array[Int]): Int = {
    a(0) * a(4) * a(8) + a(6) * a(1) * a(5) + a(3) * a(7) * a(2) - a(2) * a(4) * a(6) - a(1) * a(3) * a(8) - a(0) * a(5) * a(7)
  }

  def determinant2[T: Numeric](a: IndexedSeq[T])(implicit n: Numeric[T]): T = {
    import n._
    a(0) * a(3) - a(1) * a(2)
  }

  def determinant[T: Numeric](a: IndexedSeq[T], size: Int): T = {
    if (size == 3) {
      determinant3(a)
    } else if (size == 2) {
      determinant2(a)
    } else {
      throw new NotImplementedError
    }
  }

  def determinantArray(a: Array[Int], size: Int): Int = {
    if (size == 3) {
      determinant3Array(a)
    } else if (size == 2) {
      determinant2(a)
    } else {
      throw new NotImplementedError
    }
  }
}
