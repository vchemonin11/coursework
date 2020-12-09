package game.service


import game.db._
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by chemonin on 12.11.2020.
 */
case class UserService(db: Database)(implicit ec: ExecutionContext) {

  import UserRepository._

  def registerUser(user: User): Future[Int] = {
    runOnDb {
      for {
        inserted <- addUser(user)
      } yield inserted
    }
  }

  private def runOnDb[R, S <: NoStream, E <: Effect](f: DBIOAction[R, S, E]): Future[R] = {
    db.run(f)
    // .andThen { case _ => db.close() }
  }
}
