package game.service

import game.Move
import game.db.MoveRecord

/**
 * Created by chemonin on 15.11.2020.
 */
object Helper {
  def moveFromRecord(m : MoveRecord): Move = Move(m.x, m.y, m.value)
}
