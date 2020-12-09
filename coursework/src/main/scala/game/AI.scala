package game

/**
 * Created by chemonin on 10.11.2020.
 */
case class AI(size: Int) {
  private val size_sqr = size * size
  private val INF = 500
  private val MINUS_INF = -500

  def next(field: Field): Move = {
    val res = solve_matrix_flat(field)
    moveFromRes(res)
  }

  def moveFromRes(res: (Int, Int, Int)): Move = {
    Move(res._3 % size, res._3 / size, res._2 + 1)
  }

  private def is_first(step: Int): Boolean = step % 2 == 0

  private def predict(matrix: Array[Int], digits: Array[Boolean], step: Int, best1: Int, best2: Int): Int = {
    if (step == size_sqr) {
      // TODO fix
      // return Math.determinant3Array(matrix)
      // return Math.determinant3(matrix)
      return Math.determinantArray(matrix, size)
    }
    var best1Local = best1
    var best2Local = best2

    var digits_count = 0
    val isFirst = is_first(step)
    var k = 0
    while (k < size_sqr) {
      if (!digits(k)) {
        // optimization
        digits_count += 1
        if (digits_count == 2 && step == size_sqr - 2) {
          return if (isFirst) best2Local else best1Local
        }
        digits(k) = true
        var i: Int = 0
        while (i < size_sqr) {
          if (matrix(i) == 0) {
            matrix(i) = k + 1
            val res: Int = predict(matrix, digits, step + 1, best1Local, best2Local)

            if (isFirst) {
              if (best2Local < res) {
                best2Local = res
              }
            } else {
              if (best1Local > res) {
                best1Local = res
              }
            }

            matrix(i) = 0

            if ((!isFirst && res <= best2Local) || (isFirst && res >= best1Local)) {
              digits(k) = false
              return res
            }
          }
          i = i + 1
        }
        digits(k) = false
      }
      k += 1
    }
    if (isFirst) best2Local else best1Local
  }

  def solve_matrix_flat(matrix1: Field): (Int, Int, Int) = {

    val matrix = matrix1.arr.map {
      case Some(value) => value
      case None => 0
    }.toArray

    val digits: Array[Boolean] = new Array[Boolean](size_sqr)
    val step = matrix1.arr.flatten.map(value => {
      digits(value - 1) = true
    }).length

    var bestK = -1
    var bestPos = -1
    var best1 = INF
    var best2 = MINUS_INF
    val isFirstStep = is_first(step)
    if (step == size_sqr) {
      best1 = predict(matrix, digits, step, best1, best2)
      bestK = -1
      bestPos = -1
    }

    for (k <- 0 until size_sqr) {
      if (!digits(k)) {
        digits(k) = true
        for (i <- 0 until size_sqr) {
          if (matrix(i) == 0) {
            matrix(i) = k + 1
            val res = predict(matrix, digits, step + 1, best1, best2)
            if (isFirstStep) {
              if (best2 < res) {
                best2 = res
                bestPos = i
                bestK = k
              }
            } else {
              if (best1 > res) {
                best1 = res
                bestPos = i
                bestK = k
              }
            }

            matrix(i) = 0
          }
        }
        digits(k) = false
      }
    }
    val result = if (isFirstStep) best2 else best1
    (result, bestK, bestPos)
  }
}
