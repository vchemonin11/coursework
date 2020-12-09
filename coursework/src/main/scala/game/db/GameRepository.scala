package game.db

/**
 * Created by chemonin on 11.11.2020.
 */
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._

object GameRepository {
  val AllMoves = TableQuery[MovesTable]
  val AllGames = TableQuery[GamesTable]

  def addMove(move: MoveRecord): DIO[Int, Effect.Write] =
    AllMoves += move

  def addMoves(move: Seq[MoveRecord]): DIO[Option[Int], Effect.Write] =
    AllMoves ++= move

  def addGame(game: GameRecord): DIO[Int, Effect.Write] =
    (AllGames returning AllGames.map(_.gameId)) += game

  def updateGame(game: GameRecord): DIO[Int, Effect.Write] =
    AllGames.filter(_.gameId === game.gameId).update(game)

  def gameByKey(key: String): DIO[Option[GameRecord], Effect.Read] = {
    AllGames.filter(_.key === key).result.headOption
  }

  def gameById(gameId: Int): DIO[Option[GameRecord], Effect.Read] = {
    AllGames.filter(_.gameId === gameId).result.headOption
  }

  def gamesList(offset: Int, limit: Int): DIO[Seq[GameRecord], Effect.Read] = {
    AllGames.sortBy(_.gameId.desc).drop(offset).take(limit).result
  }

  def gamesListToJoin(offset: Int, limit: Int): DIO[Seq[GameRecord], Effect.Read] = {
    AllGames.filter(g => g.player1Id.isEmpty || g.player2Id.isEmpty ).sortBy(_.gameId.desc).drop(offset).take(limit).result
  }

  def getAllGameMoves(gameId: Int): DIO[Seq[MoveRecord], Effect.Read] =
    AllMoves.filter(_.gameId === gameId)
      .sortBy(_.moveId).result

  def getGameMoves(gameId: Int, limit: Int): DIO[Seq[MoveRecord], Effect.Read] =
    AllMoves.filter(_.gameId === gameId)
      .sortBy(_.moveId).take(limit).result
}
