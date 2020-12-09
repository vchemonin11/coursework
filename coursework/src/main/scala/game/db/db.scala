package game

import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

package object db {
  type DIO[+R, -E <: Effect] = DBIOAction[R, NoStream, E]
  val DIO = DBIO
}
