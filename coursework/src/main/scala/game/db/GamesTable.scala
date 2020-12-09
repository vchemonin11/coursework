package game.db

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape


/**
 * Created by chemonin on 12.11.2020.
 */
case class GameRecord(gameId: Int, key: String, size: Int, player1Id: Option[Int], player2Id: Option[Int])

class GamesTable(tag: Tag) extends Table[GameRecord](tag, "games") {
  def gameId = column[Int]("GAME_ID", O.PrimaryKey, O.AutoInc)
  def key = column[String]("GAME_KEY", O.Unique)
  def size = column[Int]("SIZE")
  def player1Id = column[Option[Int]]("PLAYER1_ID")
  def player2Id = column[Option[Int]]("PLAYER2_ID")
  override def * : ProvenShape[GameRecord] = (gameId, key, size, player1Id, player2Id).mapTo[GameRecord]
}
