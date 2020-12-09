package game.db

/**
 * Created by chemonin on 11.11.2020.
 */
import java.time.LocalDateTime

import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

object RaitingsRepository {
  val AllRaitings = TableQuery[RaitingsTable]

  def addScore(score: RaitingRecord): DIO[Int, Effect.Write] =
    AllRaitings += score

  def getTopNAllTime(offset: Int, limit: Int): DIO[Seq[(Int, Int)], Effect.Read] = {
    AllRaitings.groupBy(_.playerId)
      .map { case (playerId, group) => (playerId, group.map(_.score).sum.asColumnOf[Int]) }
      .sortBy { case (_, sum) => sum.desc }
      .drop(offset).take(limit)
      .result
  }

  def getRecords(offset: Int, limit: Int): DIO[Seq[(Int, Int, LocalDateTime)], Effect.Read] = {
    AllRaitings
      .sortBy (elem => (elem.score.desc, elem.time.asc))
      .map(elem => (elem.playerId, elem.score, elem.time))
      .drop(offset).take(limit)
      .result
  }

}
