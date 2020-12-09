package game.service

import java.util.UUID

import game.db.{User, UserRepository}
import org.scalatest.compatible
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future


/**
 * Created by chemonin on 14.11.2020.
 */
class UserServiceTest extends AsyncFlatSpec with Matchers {

  private val initSchema = (UserRepository.AllUsers.schema).create

  private def runMyTest(testFun: UserService => Future[compatible.Assertion]) = {
    val db = Database.forURL(
      s"jdbc:h2:mem:${UUID.randomUUID()}",
      driver = "org.h2.Driver",
      keepAliveConnection = true
    )

    db.run(
      initSchema
    )
      .flatMap(_ => {
        val userService = UserService(db)
        testFun(userService)
      })
      .andThen { case _ => db.close() }
  }

  it should "insert new user with id" in {
    runMyTest { userService =>
      val toInsert = User(10, "Alica", "alice@mail.ru", "xyz", false)
      userService.registerUser(toInsert).flatMap {
        id => {
          id shouldBe 1
        }
      }
    }
  }
}
