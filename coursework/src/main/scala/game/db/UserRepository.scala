package game.db

/**
 * Created by chemonin on 11.11.2020.
 */
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

object UserRepository {
  val AllUsers = TableQuery[UsersTable]

  def addUser(user: User): DIO[Int, Effect.Write] =
    (AllUsers returning AllUsers.map(_.id)) += user

  def userById(userId: Int): DIO[Option[User], Effect.Read] = {
    AllUsers.filter(_.id === userId).result.headOption
  }

  def getPlayers(ids: Set[Int]): DIO[Seq[(Int, String)], Effect.Read] = {
    AllUsers.filter(_.id.inSet(ids)).map(u => (u.id, u.name)).result
  }
}
