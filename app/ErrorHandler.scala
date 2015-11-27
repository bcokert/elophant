import dto.response.GenericResponse
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.Json
import javax.inject._
import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

class ErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router]
  ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Logger.error(s"An Unhandled exception occurred during request '$request'\nSTART UNHANDLED EXCEPTION: \n${exception.getMessage}\n${ExceptionUtils.getStackTrace(exception)}\nEND UNHANDLED EXCEPTION\n")
    Future.successful(InternalServerError(Json.toJson(GenericResponse(success = false, None, Some(Seq("An unexpected internal error occurred. Please contact an administrator."))))))
  }
}
