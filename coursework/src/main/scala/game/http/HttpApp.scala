package game.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import game.controller.{GameApi, GameExceptionHandler}
import game.db.{GameRepository, RaitingsRepository, User, UserRepository}
import game.service.{GameService, GameServiceImpl}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object HttpApp {
  implicit val ac: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = ac.dispatcher

  def main(args: Array[String]): Unit = {
    Await.result(DeterminantHttpGame().start(), Duration.Inf)
  }
}

case class DeterminantHttpGame()(implicit ac: ActorSystem, ec: ExecutionContext) {

  val db = Database.forConfig("h2mem1")
  import slick.jdbc.H2Profile.api._
  private val gameService = GameServiceImpl(db)
  private val gameRoute: GameApi = new GameApi(gameService)

  protected val SampleUsers = Seq(
    User(GameService.BOT_ID, "R2D2", "botsecret", "r2d2@example.com", isBot = true),
    User(GameService.ANONIMOUS_ID, "Anonymous", "anonym@example.com", "secret1", isBot = false),
    User(3, "Bob", "bob@mail.ru", "secret2", isBot = false),
    User(4, "Alice", "alice@mail.ru", "secretalice", isBot = false),
    User(5, "John", "John@mail.ru", "secretjohn", isBot = false)
  )

  private val initSchema =
    (GameRepository.AllGames.schema ++ GameRepository.AllMoves.schema ++ UserRepository.AllUsers.schema ++ RaitingsRepository.AllRaitings.schema).create


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
      .flatMap(_ => db.run(initSchema.andThen(UserRepository.AllUsers ++= SampleUsers))
      .map(_ => println("Started")))
}
