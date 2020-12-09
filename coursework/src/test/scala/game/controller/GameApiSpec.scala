package game.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import game.Field
import game.db.GameRecord
import game.models.{GameLineWithField, Player}
import game.service.GameService
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec

import scala.concurrent.Future

class GameApiSpec extends AnyFunSpec with MockFactory with ScalatestRouteTest {

  private val size: Int = 3

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  describe("GET http://localhost:8080/game/{gameId}") {
    it("возвращает текущее состояние игры с маскированным словом") {
      (mockGameService.loadGameFromDb _)
        .expects(GameService.ANONIMOUS_ID, "xyz")
        .returns(Future.successful(Some(sampleGame)))

      Get("/game/xyz") ~> route ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Option[GameDto]].contains(GameDto.fromGameLineWithField(sampleGame)))
      }
    }

    it("возвращает пустое тело, если игра не найдена") {
      (mockGameService.loadGameFromDb _)
        .expects(GameService.ANONIMOUS_ID, "xyz")
        .returns(Future.successful(None))

      Get("/game/xyz") ~> route ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Option[GameDto]].isEmpty)
      }
    }
  }

  describe("POST http://localhost:8080/game/new1/?userId={id}&size={size}") {
    it("создает новую игру") {
      (mockGameService.createGameWithPayer1 _)
        .expects(1, 3)
        .returns(Future.successful(Some(sampleRecord)))

      Post(s"/game/new1?userId=1&size=$size") ~> route ~> check {
        assert(status == StatusCodes.OK)
        assert(responseAs[Option[GameDto]].contains(GameDto.fromRecord(sampleRecord)))
      }
    }
  }

  private val mockGameService: GameService = mock[GameService]
  private val route = Route.seal(
    new GameApi(mockGameService).route
  )(exceptionHandler = GameExceptionHandler.exceptionHandler) // ExceptionHandler - обрабатывает ошибки, которые произошли при обработке запроса

  private val sampleRecord = GameRecord(12, "xyz", 3, Some(3), Some(4))

  private val sampleGame = GameLineWithField(sampleRecord.gameId, sampleRecord.key, sampleRecord.size, Some(Player(3, "Bob")), Some(Player(3, "Bob")), Field.emptyField(size), false)
}
