package game.service

import game.db._
import game.models.{GameLine, GameLineWithField, Player, TopLine}
import game.service.GameService.BOT_ID
import game.{AI, Field, Move, Rewind}
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.Database

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by chemonin on 12.11.2020.
 */
case class GameServiceImpl(db: Database)(implicit ec: ExecutionContext) extends GameService {

  import GameRepository._

  private val NON_RAITING_IDS = Vector(GameService.ANONIMOUS_ID, GameService.BOT_ID)

  def loadGameFromDb(userId: Int, gameId: String): Future[Option[GameLineWithField]] = {
    runOnDb {
      for {
        gameRecordOpt: Option[GameRecord] <- gameByKey(gameId)
        moves: Seq[MoveRecord] <- gameRecordOpt.map(gameRecord => getAllGameMoves(gameRecord.gameId)).getOrElse(DBIO.successful(Nil))
        idsOpt: Option[Set[Int]] = gameRecordOpt.map(rec => Set(rec.player1Id, rec.player2Id).flatten)
        usersPairs <- idsOpt.map(ids => UserRepository.getPlayers(ids)).getOrElse(DBIO.successful(Nil))
        field = gameRecordOpt.map(rec => {
          val usersMap: Map[Int, Seq[(Int, String)]] = usersPairs.groupBy(_._1)
          GameLineWithField(rec.gameId, rec.key, rec.size,
            rec.player1Id.map(id => Player(id, usersMap(id).head._2)),
            rec.player2Id.map(id => Player(id, usersMap(id).head._2)),
            makeFiled(rec.size, moves), !rec.player1Id.contains(userId))
        })
      } yield (field)
    }
  }

  def createGameWithPayer1(userId: Int, size: Int): Future[Option[GameRecord]] = {
    runOnDb {
      for {
        user1Opt <- UserRepository.userById(userId)
        gameRec <- user1Opt match {
          case None => DBIO.successful(None)
          case Some(user1) => {
            val gameRecord = GameRecord(0, UUID.randomUUID().toString, size, Some(userId), None)
            addGame(gameRecord).map(id => Some(gameRecord.copy(gameId = id)))
          }
        }
      } yield (gameRec)
    }
  }

  def createGameWithPayer2(userId: Int, size: Int): Future[Option[GameRecord]] = {
    runOnDb {
      for {
        user2Opt <- UserRepository.userById(userId)
        gameRec <- user2Opt match {
          case None => DBIO.successful(None)
          case Some(user2) => {
            val gameRecord = GameRecord(0, UUID.randomUUID().toString, size, None, Some(userId))
            addGame(gameRecord).map(id => Some(gameRecord.copy(gameId = id)))
          }
        }
      } yield (gameRec)
    }
  }

  def joinGame(key: String, userId: Int): Future[Option[GameRecord]] = {
    runOnDb {
      for {
        userOpt <- UserRepository.userById(userId)
        gameRecordOpt: Option[GameRecord] <- gameByKey(key)
        res: Option[GameRecord] <- gameRecordOpt.flatMap(gameRecord => userOpt.map(u => {
          if (gameRecord.player2Id.isEmpty) {
            val newRec = gameRecord.copy(player2Id = Some(u.id))
            updateGame(newRec).map(i => if (i > 0) Some(newRec) else None)
          } else if (gameRecord.player1Id.isEmpty) {
            val newRec = gameRecord.copy(player1Id = Some(u.id))
            updateGame(newRec).map(i => if (i > 0) Some(newRec) else None)
          } else {
            DBIO.successful(None)
          }
        })).getOrElse(DBIO.successful(None))
      } yield (res)
    }
  }

  def joinBot(key: String, userId: Int): Future[Option[GameRecord]] = {
    joinGame(key, GameService.BOT_ID)
  }

  def getRaitings(offset: Int, limit: Int): Future[Seq[TopLine]] = {
    runOnDb {
      for {
        list: Seq[(Int, Int, LocalDateTime)] <- RaitingsRepository.getRecords(offset, limit)
        ids: Set[Int] = list.map(_._1).toSet
        usersPairs <- UserRepository.getPlayers(ids)
        usersMap = usersPairs.groupBy(_._1)
        result = list.map(l => TopLine(l._1, usersMap(l._1).head._2, l._2, l._3))
      } yield result
    }
  }

  def moveAndBot(userId: Int, key: String, m: Move): Future[Boolean] = {
    val res = move(userId, key, m)
    res.flatMap(b => {
      if (b) {
        runOnDb {
          for {
            gameRecordOpt: Option[GameRecord] <- gameByKey(key)
            user1: Option[User] <- gameRecordOpt.flatMap(gameRecord => gameRecord.player1Id.map(p1 => UserRepository.userById(p1))).getOrElse(DBIO.successful(None))
            user2: Option[User] <- gameRecordOpt.flatMap(gameRecord => gameRecord.player2Id.map(p1 => UserRepository.userById(p1))).getOrElse(DBIO.successful(None))
            currUserOpt: Option[User] <- UserRepository.userById(userId)
            moves: Seq[MoveRecord] <- gameRecordOpt.map(gameRecord => getAllGameMoves(gameRecord.gameId)).getOrElse(DBIO.successful(Nil))
            move: Option[Move] = if (user1.get.id == BOT_ID || user2.get.id == BOT_ID) {
              val ai = AI(gameRecordOpt.get.size)
              val prevFiled = makeFiled(gameRecordOpt.get.size, moves)
              Some(ai.next(prevFiled))
            } else {
              None
            }
          } yield move
        }
      } else {
        Future.successful(None)
      }
    }).flatMap(mOpt => mOpt.map(move(BOT_ID, key, _)).getOrElse(Future.successful(false)))
    res
  }

  def move(userId: Int, key: String, m: Move) = {
    runOnDb {
      for {
        gameRecordOpt: Option[GameRecord] <- gameByKey(key)
        currUserOpt: Option[User] <- UserRepository.userById(userId)
        user1: Option[User] <- gameRecordOpt.flatMap(gameRecord => gameRecord.player1Id.map(p1 => UserRepository.userById(p1))).getOrElse(DBIO.successful(None))
        user2: Option[User] <- gameRecordOpt.flatMap(gameRecord => gameRecord.player2Id.map(p1 => UserRepository.userById(p1))).getOrElse(DBIO.successful(None))
        moves: Seq[MoveRecord] <- gameRecordOpt.map(gameRecord => getAllGameMoves(gameRecord.gameId)).getOrElse(DBIO.successful(Nil))
        record: Option[(MoveRecord, Option[Int], Boolean, GameRecord)] = gameRecordOpt.flatMap(gameRecord => user1.map(u1 => user2.map(u2 => currUserOpt.flatMap(currUser => {
          val isFirst = moves.length % 2 == 0
          val isSameUser = if (isFirst) u1.id == userId else u2.id == userId
          if (!isSameUser) {
            None
          } else {
            val prevFiled = makeFiled(gameRecord.size, moves)
            val newField = prevFiled.makeMove(m)
            if (prevFiled.moveCount == newField.moveCount) {
              None
            } else {
              val mRecord = MoveRecord(0, gameRecord.gameId, newField.moveCount, userId, m.x, m.y, m.value)
              Some(mRecord, newField.resultOpt, isFirst, gameRecord)
            }
          }
        })))).flatten.flatten


        res <- record.map(rec => {
          val rait = rec._2.map(det => {
            val d = LocalDateTime.now()
            if (!NON_RAITING_IDS.contains(rec._4.player1Id.get)) {
              val r1 = RaitingRecord(0, rec._4.gameId, rec._4.player1Id.get, det, det, d)
              if (!NON_RAITING_IDS.contains(rec._4.player2Id.get)) {
                val r2 = RaitingRecord(0, rec._4.gameId, rec._4.player2Id.get, det, -det, d)
                RaitingsRepository.addScore(r1).andThen(RaitingsRepository.addScore(r2))
              } else {
                RaitingsRepository.addScore(r1)
              }
            } else {
              if (!NON_RAITING_IDS.contains(rec._4.player2Id.get)) {
                val r2 = RaitingRecord(0, rec._4.gameId, rec._4.player2Id.get, det, -det, d)
                RaitingsRepository.addScore(r2)
              } else {
                DBIO.successful(0)
              }
            }
          }).getOrElse(DBIO.successful(0))
          rait.andThen(addMove(rec._1))
        }).getOrElse(DBIO.successful(0))
      } yield (res > 0)
    }
  }

  def rewind(userId: Int, key: String, r: Rewind): Future[Option[GameLineWithField]] = {
    runOnDb {
      for {
        gameRecordOpt: Option[GameRecord] <- gameByKey(key)
        moves: Seq[MoveRecord] <- gameRecordOpt.map(gameRecord => getGameMoves(gameRecord.gameId, r.move)).getOrElse(DBIO.successful(Nil))
        idsOpt: Option[Set[Int]] = gameRecordOpt.map(rec => Set(rec.player1Id, rec.player2Id).flatten)
        usersPairs <- idsOpt.map(ids => UserRepository.getPlayers(ids)).getOrElse(DBIO.successful(Nil))
        record = gameRecordOpt.flatMap(gameRecord => {
          if (r.move == moves.length) {
            val prevFiled = makeFiled(gameRecord.size, moves.take(r.move))
            val usersMap: Map[Int, Seq[(Int, String)]] = usersPairs.groupBy(_._1)
            val gameWithField = GameLineWithField(gameRecord.gameId, gameRecord.key, gameRecord.size,
              gameRecord.player1Id.map(id => Player(id, usersMap(id).head._2)),
              gameRecord.player2Id.map(id => Player(id, usersMap(id).head._2)),
              prevFiled, gameRecord.player2Id.contains(userId)
            )
            Some(gameWithField)
          } else {
            None
          }
        })
      } yield (record)
    }
  }

  def forkGame(userId: Int, key: String, r: Rewind): Future[Option[GameRecord]] = {
    runOnDb {
      for {
        gameRecordOpt: Option[GameRecord] <- gameByKey(key)
        userOpt <- UserRepository.userById(userId)
        moves: Seq[MoveRecord] <- gameRecordOpt.map(gameRecord => getGameMoves(gameRecord.gameId, r.move)).getOrElse(DBIO.successful(Nil))
        record <- gameRecordOpt.flatMap(gameRecord => userOpt.map(u => {
          if (r.move == moves.length) {
            val gameRecordNew = if (r.move % 2 == 0) {
              GameRecord(0, UUID.randomUUID().toString, gameRecord.size, Some(userId), None)
            } else {
              GameRecord(0, UUID.randomUUID().toString, gameRecord.size, None, Some(userId))
            }
            addGame(gameRecordNew).map(id => Some(gameRecordNew.copy(gameId = id)))
          } else {
            DBIO.successful(None)
          }
        })).getOrElse(DBIO.successful(None))
        newMoves <- record.map(r => {
          val newM = moves.map(m => m.copy(gameId = r.gameId))
          addMoves(newM)
        }).getOrElse(DBIO.successful(Some(0)))
      } yield (record)
    }
  }

  def gamesList(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]] = {
    gamesList(offset, limit, GameRepository.gamesList _)
  }

  def gamesListToJoin(userId: Int, offset: Int, limit: Int): Future[Seq[GameLine]] = {
    gamesList(offset, limit, GameRepository.gamesListToJoin _)
  }

  private def gamesList(offset: Int, limit: Int, gameFetcher: (Int, Int) => DIO[Seq[GameRecord], Effect.Read]): Future[Seq[GameLine]] = {
    runOnDb {
      for {
        list: Seq[GameRecord] <- gameFetcher(offset, limit)
        ids: Set[Int] = list.flatMap(rec => Seq(rec.player1Id, rec.player2Id)).flatten.toSet
        usersPairs <- UserRepository.getPlayers(ids)
        usersMap = usersPairs.groupBy(_._1)
        games = list.map(rec => GameLine(rec.gameId, rec.key, rec.size,
          rec.player1Id.map(id => Player(id, usersMap(id).head._2)),
          rec.player2Id.map(id => Player(id, usersMap(id).head._2))
        ))

      } yield games
    }
  }


  private def makeFiled(size: Int, moves: Seq[MoveRecord]) = {
    val f = Field.emptyField(size)
    moves.foldLeft(f)((f, m) => f.makeMove(Helper.moveFromRecord(m)))
  }

  private def runOnDb[R, S <: NoStream, E <: Effect](f: DBIOAction[R, S, E]): Future[R] = {
    db.run(f)
    // .andThen { case _ => db.close() }
  }
}
