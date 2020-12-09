package game.models

import game.Field

/**
 * Created by chemonin on 05.12.2020.
 */
case class GameLineWithField(gameId: Int, key: String, size: Int,
                             player1: Option[Player], player2: Option[Player], field: Field, startRed: Boolean)
