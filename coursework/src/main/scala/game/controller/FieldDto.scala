package game.controller

import game.Field
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


/**
 * Created by chemonin on 27.11.2020.
 */
case class FieldDto(arr: Vector[Option[Int]], size: Int)

object FieldDto {
  implicit val jsonDecoder: Decoder[FieldDto] = deriveDecoder
  implicit val jsonEncoder: Encoder[FieldDto] = deriveEncoder
  def fromRecord(rec : Field): FieldDto = FieldDto(rec.arr, rec.size)
}
