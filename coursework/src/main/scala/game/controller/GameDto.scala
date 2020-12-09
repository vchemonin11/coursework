package game.controller

import game.db.GameRecord
import game.models.{GameLine, GameLineWithField, Player, TopLine}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.format.DateTimeFormatter

/**
 * Created by chemonin on 27.11.2020.
 */

case class GameDto(gameId: Int, key: String, size: Int, player1: Option[PlayerDto], player2: Option[PlayerDto],
                   arr: Seq[Option[Int]], result: Option[Int], isRed: Boolean)

case class PlayerDto(id: Int, name: String)

case class TopLineDto(id: Int, name: String, result: Int, date: String)

object TopLineDto {
  implicit val jsonDecoder: Decoder[TopLineDto] = deriveDecoder
  implicit val jsonEncoder: Encoder[TopLineDto] = deriveEncoder
  def fromTopLine(line: TopLine): TopLineDto = TopLineDto(line.id, line.name, line.result,
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(line.date))
}

object PlayerDto {
  implicit val jsonDecoder: Decoder[PlayerDto] = deriveDecoder
  implicit val jsonEncoder: Encoder[PlayerDto] = deriveEncoder
  def fromPlayer(player: Player): PlayerDto = PlayerDto(player.id, player.name)
}

object GameDto {
  implicit val jsonDecoder: Decoder[GameDto] = deriveDecoder
  implicit val jsonEncoder: Encoder[GameDto] = deriveEncoder
  def fromRecord(rec : GameRecord): GameDto = GameDto(rec.gameId, rec.key, rec.size,
    rec.player1Id.map(id => PlayerDto.fromPlayer(Player(id, "Stub"))),
    rec.player2Id.map(id => PlayerDto.fromPlayer(Player(id, "Stub"))), Nil, None, false)

  def fromGameLine(rec : GameLine): GameDto = GameDto(rec.gameId, rec.key, rec.size,
    rec.player1.map(PlayerDto.fromPlayer),
    rec.player2.map(PlayerDto.fromPlayer),  Nil, None, false)

  def fromGameLineWithField(rec : GameLineWithField): GameDto = GameDto(rec.gameId, rec.key, rec.size,
    rec.player1.map(PlayerDto.fromPlayer),
    rec.player2.map(PlayerDto.fromPlayer), rec.field.arr, rec.field.resultOpt, rec.startRed)
}
