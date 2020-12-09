package game.controller

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler

object GameExceptionHandler {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e => complete(BadRequest, ExceptionResponse(e.getMessage))
    }
}
