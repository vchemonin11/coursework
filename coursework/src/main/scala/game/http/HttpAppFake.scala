package game.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import game.controller.{GameApi, GameExceptionHandler}
import game.db.GameRecord
import game.models.{GameLine, GameLineWithField, Player, TopLine}
import game.service.GameService
import game.{Field, Move, Rewind}

import java.time.LocalDateTime
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object HttpAppFake {
  implicit val ac: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = ac.dispatcher

  def main(args: Array[String]): Unit = {
    Await.result(DeterminantHttpGameFake().start(), Duration.Inf)
  }
}


class GameServiceFake() extends GameService {
  override def loadGameFromDb(userId: Int, gameId: String): Future[Option[GameLineWithField]] = {
    val size = 3
    val win1Field = Field(Vector(1, 9, 6, 8, 5, 0, 0, 3, 2).map(i => if (i> 0) Some(i) else None), size)
    val gameLineWithField: GameLineWithField = GameLineWithField(12, "dasd-dadad-dada", size, Some(Player(3, "Max")), None, win1Field, true)
    Future.successful(Some(gameLineWithField))
  }

  override def createGameWithPayer1(userId: Int, size: Int): Future[Option[GameRecord]] = ???

  override def createGameWithPayer2(userId: Int, size: Int): Future[Option[GameRecord]] = ???

  override def joinGame(key: String, userId: Int): Future[Option[GameRecord]] = ???

  override def joinBot(key: String, userId: Int): Future[Option[GameRecord]] = ???

  override def getRaitings(offset: Int, limit: Int): Future[Seq[TopLine]] = {
    val line1 = TopLine(3, "Max", 40, LocalDateTime.now())
    val line2 = TopLine(4, "Bob", -40, LocalDateTime.now())
    Future.successful(List(line1, line2))
  }

  override def moveAndBot(userId: Int, key: String, m: Move): Future[Boolean] = ???

  override def rewind(userId: Int, key: String, r: Rewind): Future[Option[GameLineWithField]] = ???

  override def forkGame(userId: Int, key: String, r: Rewind): Future[Option[GameRecord]] = ???

  override def gamesList(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]] = ???

  override def gamesListToJoin(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]] = ???
}

case class DeterminantHttpGameFake()(implicit ac: ActorSystem, ec: ExecutionContext) {

  private val gameService = new GameServiceFake()
  private val gameRoute: GameApi = new GameApi(gameService)

  private val routes = Route.seal(
    RouteConcatenation.concat(
      gameRoute.route
    )
  )(
    exceptionHandler = GameExceptionHandler.exceptionHandler // Обрабатывает ошибки, возникающие при обработке запроса
  )

  def start(): Future[Unit] =
    Http()
      .newServerAt("localhost", 8080)
      .bind(routes)
      .map { case b => println(s"bind port at: $b") }
      .map(_ => println("Started"))
}
