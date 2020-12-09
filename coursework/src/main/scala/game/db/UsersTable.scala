package game.db

import slick.jdbc.H2Profile.api._
import slick.lifted.ProvenShape

case class User(id: Int, name: String, email: String, password: String, isBot: Boolean)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {
  def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name: Rep[String] = column[String]("name")
  def email: Rep[String] = column[String]("email")
  def password: Rep[String] = column[String]("password")
  def isBot: Rep[Boolean] = column[Boolean]("is_bot")

  override def * : ProvenShape[User] = (id, name, email, password, isBot).mapTo[User]
}
