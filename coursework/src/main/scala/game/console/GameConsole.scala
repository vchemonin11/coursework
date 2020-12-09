package game.console

import Determinant.program
import monix.execution.Scheduler

import scala.concurrent.duration.Duration

/**
 * Created by chemonin on 10.11.2020.
 */
object GameConsole {
  val size = 3

  def main(args: Array[String]): Unit = {
    import Scheduler.Implicits.global
    program(size).runSyncUnsafe(Duration.Inf)
  }
}
