package game.controller

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import game.{Move, Rewind}
import game.service.GameService

import scala.concurrent.ExecutionContext

class GameApi(gameService: GameService)(implicit ec: ExecutionContext) {

  import akka.http.scaladsl.server.Directives._
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport.marshaller

  private def moveFromParams(x: Int, y: Int, digit: Int): Move = {
    Move(x, y, digit)
  }

  private val cors = new CORSHandler {}

  private def corsComplete(m: => ToResponseMarshallable): Route = cors.corsHandler(complete(m))

  private val rewind = (get & path("game" / Segment / "rewind" / IntNumber) & parameter("userId".withDefault(GameService.ANONIMOUS_ID.toString)) ) {
    (key, rewind, userId) => corsComplete(gameService.rewind(userId.toInt, key, Rewind(rewind)).map(_.map(GameDto.fromGameLineWithField)))
  }

  private val newAsPlayer1 = (post & path("game" / "new1") & parameter("userId") & parameter("size")) { (userId, size) =>
    corsComplete(gameService.createGameWithPayer1(userId.toInt, size.toInt).map({_.map(GameDto.fromRecord)}))
  }

  private val newAsPlayer2 = (post & path("game" / "new2") & parameter("userId") & parameter("size")) { (userId, size) =>
    corsComplete(gameService.createGameWithPayer2(userId.toInt, size.toInt).map(_.map(GameDto.fromRecord)))
  }

  private val join = (post & path("game" / "join" / Segment) & parameter("userId")) { (key, userId) =>
    corsComplete(gameService.joinGame(key, userId.toInt).map(_.map(GameDto.fromRecord)))
  }

  private val joinbot = (post & path("game" / "joinbot" / Segment) & parameter("userId")) { (key, userId) =>
    corsComplete(gameService.joinBot(key, userId.toInt).map(_.map(GameDto.fromRecord)))
  }


  private val fork = (post & path("game" / "fork" / Segment) & parameter("userId") & parameter("rewind")) { (key, userId, rewind) =>
    corsComplete(gameService.forkGame(userId.toInt, key, Rewind(rewind.toInt)).map(_.map(GameDto.fromRecord)))
  }


  private val move = (post & path("game" / Segment / "move") & parameter("userId") & parameter("x")
    & parameter("y") & parameter("digit")) { (id, userId, x, y, digit) =>
    corsComplete(gameService.moveAndBot(userId.toInt, id, moveFromParams(x.toInt, y.toInt, digit.toInt)))
  }

  private val find = (get & path("game" / Segment) & parameter("userId".withDefault(GameService.ANONIMOUS_ID.toString))) { (id, userId) =>
    corsComplete(gameService.loadGameFromDb(userId.toInt, id).map(_.map(GameDto.fromGameLineWithField)))
  }

  private val raitingsAll = (get & path("raitings" / "all") & parameter("offset")) { offset =>
    corsComplete(gameService.getRaitings(offset.toInt, 20).map(_.map(TopLineDto.fromTopLine)))
  }

  private val list = (get & path("game" / "list") & parameter("userId")) { userId =>
    corsComplete(gameService.gamesList(userId.toInt, 0, 20).map(_.map(GameDto.fromGameLine)))
  }

  private val joinlist = (get & path("game" / "joinlist") & parameter("userId")) { userId =>
    corsComplete(gameService.gamesListToJoin(userId.toInt, 0, 20).map(_.map(GameDto.fromGameLine)))
  }


  val route: Route = options {
    cors.corsHandler(complete(StatusCodes.OK))
  }  ~ newAsPlayer1 ~ newAsPlayer2 ~ join ~ joinbot ~ fork ~ rewind ~ list ~ joinlist ~ find ~ move ~ raitingsAll
}
