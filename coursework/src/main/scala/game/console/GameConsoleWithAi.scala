package game.console

import Determinant.programWithAi
import monix.execution.Scheduler

import scala.concurrent.duration.Duration

/**
 * Created by chemonin on 10.11.2020.
 */
object GameConsoleWithAi {
  val size = 3

  def main(args: Array[String]): Unit = {
    import Scheduler.Implicits.global
    programWithAi(size, false).runSyncUnsafe(Duration.Inf)
  }
}
