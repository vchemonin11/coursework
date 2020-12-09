package game.db

import java.time.LocalDateTime
import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape


/**
 * Created by chemonin on 12.11.2020.
 */
case class RaitingRecord(id: Int, gameId: Int, playerId: Int, determinant: Int, score: Int, time: LocalDateTime)

class RaitingsTable(tag: Tag) extends Table[RaitingRecord](tag, "raitings") {
  def id = column[Int]("RECORD_ID", O.PrimaryKey, O.AutoInc)
  def gameId = column[Int]("GAME_ID")
  def playerId = column[Int]("USER_ID")
  def determinant = column[Int]("DETERMINANT")
  def score = column[Int]("SCORE")
  def time = column[LocalDateTime]("TIME")
  override def * : ProvenShape[RaitingRecord] = (id, gameId, playerId, determinant, score, time).mapTo[RaitingRecord]
}
