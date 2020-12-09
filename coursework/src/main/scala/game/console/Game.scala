package game.console

import game.{Field, Move, Rewind}

/**
 * Created by chemonin on 08.11.2020.
 */
case class Game(player1: Player, player2: Player, fields: Vector[Field], currentMove: Int) {

  def currentField: Field = fields(currentMove)

  def printGame(): Unit = {
    currentField.printField()
  }

  def makeMove(m: Move): Game = {
    val newField = currentField.makeMove(m)
    if (newField.moveCount == currentField.moveCount) {
      this
    } else {
      if (fields.length == currentMove + 1) {
        this.copy(fields = fields.appended(newField), currentMove = currentMove + 1)
      } else {
        val newFields = fields.take(currentMove + 1).appended(newField)
        this.copy(fields = newFields, currentMove = currentMove + 1)
      }
    }
  }

  def resultOpt: Option[Int] = currentField.resultOpt

  def rewind(rewind: Rewind): Option[Game] = {
    val moveNumber = rewind.move
    if(moveNumber>=0 && moveNumber < fields.length) {
        Some(this.copy(currentMove = moveNumber))
    } else {
      None
    }
  }

  def currentPlayerName: String = if (isFirstPlayer) player1.name else player2.name

  def isFirstPlayer: Boolean = currentField.moveCount % 2 == 0

}

object Game {
  def initGame(player1: Player, player2: Player, size: Int): Game =
    Game(player1, player2, Vector(emptyField(size)), 0)

  def emptyField(size: Int): Field = {
    Field(Vector.fill(size * size)(None), size)
  }
}
