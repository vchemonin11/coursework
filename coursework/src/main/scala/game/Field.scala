package game

/**
 * Created by chemonin on 08.11.2020.
 */
case class Field(arr: Vector[Option[Int]], size: Int) {
  def printField(): Unit = {
    for (i <- 0 until size) {
      print("|")
      for (j <- 0 until size) {
        val el = arr(i * size + j)
        el match {
          case Some(value) => print(value)
          case None => print(" ")
        }
        if (j < size - 1) {
          print(" ")
        }
      }
      println("|")
    }
  }

  def isComlete: Boolean = {
    arr.forall(_.isDefined)
  }

  private def inBounds(value: Int) = value > 0 && value <= size * size

  private def inField(value: Int) = value >=0 && value < size

  private def alreadyUsed(value: Int) = arr.contains(Some(value))

  def moveCount: Int = arr.count(_.isDefined)

  private def result: Int = {
    Math.determinant(arr.flatten, size)
  }

  def resultOpt: Option[Int] = {
    if (isComlete) {
      Some(result)
    } else {
      None
    }
  }

  def makeMove(m: Move): Field = {
    if (!inField(m.x) || !inField(m.y)) {
      return this
    }
    val index = m.y * size + m.x
    val el = arr(index)
    el match {
      case Some(_) => this
      case None =>
        if (!inBounds(m.value) || alreadyUsed(m.value)) {
          this
        } else {
          val updated = arr.updated(index, Some(m.value))
          Field(updated, size)
        }

    }
  }
}

object Field {
  def emptyField(size: Int): Field = {
    Field(Vector.fill(size * size)(None), size)
  }
}
