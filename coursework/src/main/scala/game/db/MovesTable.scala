package game.db

/**
 * Created by chemonin on 11.11.2020.
 */

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape



case class MoveRecord(moveId: Int,
                      gameId: Int,
                      step: Int,
                      playerId: Int,
                      x: Int,
                      y: Int,
                      value: Int)

class MovesTable(tag: Tag) extends Table[MoveRecord](tag, "moves") {
  def moveId = column[Int]("MOVE_ID", O.PrimaryKey, O.AutoInc)
  def gameId = column[Int]("GAME_ID")
  def step = column[Int]("STEP")
  def playerId = column[Int]("PLAYER_ID")
  def x = column[Int]("X")
  def y = column[Int]("Y")
  def value = column[Int]("VALUE")
  override def * : ProvenShape[MoveRecord] = (moveId, gameId, step, playerId, x, y, value).mapTo[MoveRecord]
}
