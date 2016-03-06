import dto.response.GenericResponse
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.Json
import javax.inject._
import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import play.api.Logger
import scala.concurrent._
import play.api.http.Status._
import views.html.defaultpages

class ErrorHandler @Inject()(
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Logger.error(s"An Unhandled exception occurred during request '$request'\nSTART UNHANDLED EXCEPTION: \n${exception.getMessage}\n${ExceptionUtils.getStackTrace(exception)}\nEND UNHANDLED EXCEPTION\n")
    Future.successful(InternalServerError(Json.toJson(GenericResponse(success = false, None, Some(Seq("An unexpected internal error occurred. Please contact an administrator."))))))
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.warn(s"Client Error '$statusCode' for request '$request'")
    Future.successful(env.mode match {
      case Mode.Prod => statusCode match {
        case BAD_REQUEST => BadRequest(Json.toJson(GenericResponse(success = false, None, Some(Seq("Bad request.")))))
        case FORBIDDEN => Forbidden(Json.toJson(GenericResponse(success = false, None, Some(Seq("The requested resource is forbidden.")))))
        case NOT_FOUND => NotFound(Json.toJson(GenericResponse(success = false, None, Some(Seq("The requested resource does not exist.")))))
      }
      case Mode.Dev => statusCode match {
        case BAD_REQUEST => BadRequest(Json.toJson(GenericResponse(success = false, None, Some(Seq("Bad request.")))))
        case FORBIDDEN => Forbidden(Json.toJson(GenericResponse(success = false, None, Some(Seq("The requested resource is forbidden.")))))
        case NOT_FOUND => NotFound(defaultpages.devNotFound(request.method, request.uri, Some(router.get)))
      }
    })
  }
}
