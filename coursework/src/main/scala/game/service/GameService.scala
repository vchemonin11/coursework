package game.service

import game.db.GameRecord
import game.models.{GameLine, GameLineWithField, TopLine}
import game.{Move, Rewind}

import scala.concurrent.Future

/**
 * Created by chemonin on 12.11.2020.
 */
trait GameService {

  def loadGameFromDb(userId: Int, gameId: String): Future[Option[GameLineWithField]]

  def createGameWithPayer1(userId: Int, size: Int): Future[Option[GameRecord]]

  def createGameWithPayer2(userId: Int, size: Int): Future[Option[GameRecord]]

  def joinGame(key: String, userId: Int): Future[Option[GameRecord]]
  def joinBot(key: String, userId: Int): Future[Option[GameRecord]]
  def getRaitings(offset: Int, limit: Int): Future[Seq[TopLine]]
  def moveAndBot(userId: Int, key: String, m: Move): Future[Boolean]
  def rewind(userId: Int, key: String, r: Rewind): Future[Option[GameLineWithField]]
  def forkGame(userId: Int, key: String, r: Rewind): Future[Option[GameRecord]]
  def gamesList(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]]
  def gamesListToJoin(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]]
}

object GameService {
  val BOT_ID = 1
  val ANONIMOUS_ID = 2
}
