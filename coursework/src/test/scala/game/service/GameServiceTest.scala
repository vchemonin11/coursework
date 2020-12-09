package game.service

import java.util.UUID

import game.{Move, Rewind}
import game.db.{GameRepository, RaitingsRepository, User, UserRepository}
import org.scalatest.compatible
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future


/**
 * Created by chemonin on 14.11.2020.
 */
class GameServiceTest extends AsyncFlatSpec with Matchers {

  private val initSchema =
    (GameRepository.AllGames.schema ++ GameRepository.AllMoves.schema ++ UserRepository.AllUsers.schema ++ RaitingsRepository.AllRaitings.schema).create

  private val bobId = 3
  private val aliceId = 4
  private val johnId = 5

  protected val SampleUsers = Seq(
    User(GameService.BOT_ID, "R2D2", "botsecret", "r2d2@example.com", isBot = true),
    User(GameService.ANONIMOUS_ID, "Anonymous", "anonym@example.com", "secret1", isBot = false),
    User(bobId, "Bob", "bob@mail.ru", "secret2", isBot = false),
    User(aliceId, "Alice", "alice@mail.ru", "secretalice", isBot = false),
    User(johnId, "John", "John@mail.ru", "secretjohn", isBot = false)
  )


  private def runMyTest(testFun: GameServiceImpl => Future[compatible.Assertion]) = {
    val db = Database.forURL(
      s"jdbc:h2:mem:${UUID.randomUUID()}",
      driver = "org.h2.Driver",
      keepAliveConnection = true
    )

    db.run(
      initSchema.andThen(UserRepository.AllUsers ++= SampleUsers)
    )
      .flatMap(_ => {
        val gameService = GameServiceImpl(db)
        testFun(gameService)
      })
      .andThen { case _ => db.close() }
  }



  it should "be normal game" in {
    runMyTest { gameService => gameService.createGameWithPayer1(bobId, 3).flatMap {
      p => p.isDefined shouldBe true

      val gameRecord = p.get
      gameRecord.player1Id shouldBe Some(bobId)
      gameService.joinGame(gameRecord.key, aliceId).flatMap(b => {
        b.get.player2Id shouldBe Some(aliceId)

        gameService.loadGameFromDb(bobId, gameRecord.key).map(optGame => {
          optGame.isDefined shouldBe false
        })

        for {
          move1 <- gameService.move(bobId, gameRecord.key, Move(0, 0, 5))
          move2 <- gameService.move(aliceId, gameRecord.key, Move(1, 0, 9))
          move3 <- gameService.move(bobId, gameRecord.key, Move(0, 2, 4))
          move4OutOfField <- gameService.move(aliceId, gameRecord.key, Move(0, 3, 4))
          move4SameUserTwice <- gameService.move(bobId, gameRecord.key, Move(1, 1, 1))
          move4SameValueTwice <- gameService.move(aliceId, gameRecord.key, Move(0, 2, 4))
          move4 <- gameService.move(aliceId, gameRecord.key, Move(1, 1, 1))
          move5 <- gameService.move(bobId, gameRecord.key, Move(2, 2, 2))
          move6 <- gameService.move(aliceId, gameRecord.key, Move(2, 0, 3))
          move7 <- gameService.move(bobId, gameRecord.key, Move(1, 2, 6))
          move8 <- gameService.move(aliceId, gameRecord.key, Move(2, 1, 7))
          move9 <- gameService.move(bobId, gameRecord.key, Move(0, 1, 8))
          raitings <- gameService.getRaitings(0, 10)
        }  yield {
          move1 shouldBe true
          move2 shouldBe true
          move3 shouldBe true
          move4OutOfField shouldBe false
          move4SameUserTwice shouldBe false
          move4SameValueTwice shouldBe false
          move4 shouldBe true
          move5 shouldBe true
          move6 shouldBe true
          move7 shouldBe true
          move8 shouldBe true
          move9 shouldBe true
          raitings.length shouldBe 2
          raitings(0).id shouldBe bobId
          raitings(1).id shouldBe aliceId
          raitings(0).result shouldBe 40
          raitings(1).result shouldBe -40
        }
      })
    }}
  }

  it should "fork game" in {
    runMyTest { gameService => gameService.createGameWithPayer1(bobId, 3).flatMap {
      p =>

        val gameRecord = p.get
        gameRecord.player1Id shouldBe Some(bobId)
        gameService.joinGame(gameRecord.key, aliceId).flatMap(b => {
          b.get.player2Id shouldBe Some(aliceId)
          for {
            move1 <- gameService.move(bobId, gameRecord.key, Move(0, 0, 5))
            move2 <- gameService.move(aliceId, gameRecord.key, Move(1, 0, 9))
            move3 <- gameService.move(bobId, gameRecord.key, Move(0, 2, 4))
            move4 <- gameService.move(aliceId, gameRecord.key, Move(1, 1, 1))
            move5 <- gameService.move(bobId, gameRecord.key, Move(2, 2, 2))
            move6 <- gameService.move(aliceId, gameRecord.key, Move(2, 0, 3))
            afterFork <- gameService.forkGame(johnId, gameRecord.key, Rewind(4))
            afterForkAndJoin <- gameService.joinGame(afterFork.get.key, bobId)
            forkedField <- gameService.loadGameFromDb(johnId, afterForkAndJoin.get.key)
          }  yield {
            move1 shouldBe true
            move2 shouldBe true
            move3 shouldBe true
            move4 shouldBe true
            move5 shouldBe true
            move6 shouldBe true
            forkedField.get.field.moveCount shouldBe 4
          }
        })
    }}
  }
}
