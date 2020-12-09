package game.console

import game._
import monix.eval.Task

import scala.io.StdIn

object Console {
  def putStrLn(string: String): Task[Unit] = Task(println(string))

  val getStrLn: Task[String] = Task(StdIn.readLine())
  val getChar: Task[Char] = Task(StdIn.readChar())
}

object Determinant {

  def getName(index: Int): Task[String] = Console.putStrLn(s"Enter your name($index)").flatMap(_ => Console.getStrLn)

  def winningMessage(game: Game, result: Int): String = {
    val namedMessage = if (result < 0) {
      s"${game.player2.name} wins"
    } else if (result > 0) {
      s"${game.player1.name} wins"
    } else {
      "Draw"
    }
    s"Determinant =  ${result}. ${namedMessage}"
  }


  def getCommand(userInput: String) : Command = {
     val exitRegexp = "exit".r
     val moveRegexp = "m \\d \\d \\d".r
     val rewindRegexp = "r \\d".r
    userInput match {
      case exitRegexp() => Exit
      case moveRegexp() => {
        val digits = userInput.split(" ").drop(1).map(_.toInt)
        Move(digits(0), digits(1), digits(2))
      }
      case rewindRegexp() => {
        val digits = userInput.split(" ").drop(1).map(_.toInt)
        Rewind(digits(0))
      }
      case _ => Invalid
    }
  }

  def getChoice(game: Game): Task[Command] =
    (if (game.resultOpt.isDefined) Console.putStrLn(s"You can rewind")
      else Console.putStrLn(s"Make move or rewind, ${game.currentPlayerName}"))
    .flatMap(_ => Console.getStrLn.flatMap(s => Task(getCommand(s))))

  def renderState(state: Game): Task[Unit] = {
    state.resultOpt match {
      case None => Task(state.printGame())
      case Some(result) => Task(state.printGame()).flatMap(_ => Console.putStrLn(winningMessage(state, result)))
    }
  }

  def gameLoop(oldState: Game): Task[Unit] = {
    renderState(oldState).flatMap(_ => getChoice(oldState).flatMap {
      case c: Move => {
        val newState = oldState.makeMove(c)
        gameLoop(newState)
      }
      case r: Rewind => {
        val newStateOpt = oldState.rewind(r)
        newStateOpt match {
          case Some(newState) => gameLoop(newState)
          case None => Console.putStrLn("Invalid rewind").flatMap(_ => renderState(oldState).flatMap(_ => gameLoop(oldState)))
        }
      }
      case Exit => Console.putStrLn("Buy")
      case Invalid => Console.putStrLn("Invalid command").flatMap(_ => gameLoop(oldState))
    })
  }

  def gameLoopWithAi(oldState: Game, ai: AI, isAiMove: Boolean): Task[Unit] = {
    renderState(oldState).flatMap(_ => if (isAiMove) {
      val newState = oldState.makeMove(ai.next(oldState.currentField))
      gameLoopWithAi(newState, ai, false)
    } else {
      getChoice(oldState).flatMap {
      case c: Move => {
        val newState = oldState.makeMove(c)
        gameLoopWithAi(newState, ai, newState.currentMove != oldState.currentMove)
      }
      case r: Rewind => {
        val newStateOpt = oldState.rewind(r)
        newStateOpt match {
          case Some(newState) => {
            gameLoopWithAi(newState, ai, false)
          }
          case None => Console.putStrLn("Invalid rewind").flatMap(_ => gameLoopWithAi(oldState, ai, false))
        }
      }
      case Exit => Console.putStrLn("Buy")
      case Invalid => Console.putStrLn("Invalid command").flatMap(_ => gameLoopWithAi(oldState, ai, false))
    }
    })
  }

  def program(size: Int): Task[Unit] =
    for {
      _ <- Console.putStrLn("Game for two people")
      name1 <- getName(1)
      name2 <- getName(2)
      game = Game.initGame(Player(name1), Player(name2), size)
      _ <- gameLoop(game)
    } yield ()

  def programWithAi(size: Int, isAiMove: Boolean): Task[Unit] = {
    val bot = AI(size)
    for {
      _ <- Console.putStrLn("Game with AI started")
      name1 <- getName(1)
      name2 = "R2D2"
      game = if (!isAiMove) Game.initGame(Player(name1), Player(name2), size) else Game.initGame(Player(name2), Player(name1), size)
      _ <- gameLoopWithAi(game, bot, isAiMove)
    } yield ()
  }

}
