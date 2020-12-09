package game

/**
 * Created by chemonin on 10.11.2020.
 */
sealed trait Command
case class Move(x: Int, y: Int, value: Int) extends Command
case class Rewind(move: Int) extends Command
case object Exit extends Command
case object Invalid extends Command
