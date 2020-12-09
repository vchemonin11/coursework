package game.models

/**
 * Created by chemonin on 05.12.2020.
 */
case class GameLine(gameId: Int, key: String, size: Int,
                    player1: Option[Player], player2: Option[Player])
