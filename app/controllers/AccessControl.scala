package controllers

import dto.response.{GenericResponse, AccessControlFailureResponse}
import exception.AppNotFoundException
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import dao.{AppsDao, PermissionsDao}
import types.{PermissionLevel, PermissionType, PermissionLevels}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

case class InadequatePermissions(permType: PermissionType, currentLevel: PermissionLevel, minimumLevel: PermissionLevel) {
  override def toString = s"$permType requires $minimumLevel, given $currentLevel"
}

trait AccessControl extends Controller {

  case class AccessControlledAction[A](minimumAccessRequirements: Map[PermissionType, PermissionLevel])(action: Action[A]) extends Action[A] {
    def apply(request: Request[A]): Future[Result] = {
      val authToken = request.headers.get("Auth-Token").getOrElse("No Token Provided")
      Logger.info(s"ACCESS CONTROL: [$authToken] wants to access ${request.method} ${request.path}, which has minimum permissions $minimumAccessRequirements")

      request.headers.get("Auth-Token") match {
        case None =>
          Logger.info("ACCESS CONTROL: No auth token was provided")
          Future.successful(Forbidden(Json.toJson(GenericResponse(success = false, None, Some(Seq("No auth token was provided. Please provide a valid auth token in the 'Auth-Token' http header"))))).as("application/json"))

        case Some(authToken: String) =>
          val inadequatePermissionsFuture = for {
            app <- AppsDao.getAppByToken(authToken)
            permissionsPerType <- PermissionsDao.getPermissions(minimumAccessRequirements.keys, app.id)
            currentLevelsWithMinimumLevels = minimumAccessRequirements.keys.map(permType => (permType, permissionsPerType.getOrElse(permType, PermissionLevels.NONE), minimumAccessRequirements(permType)))
          } yield currentLevelsWithMinimumLevels.filter {
              case (permType: PermissionType, current: PermissionLevel, minimum: PermissionLevel) => current < minimum
            }.map {
              case (permType: PermissionType, current: PermissionLevel, minimum: PermissionLevel) => InadequatePermissions(permType, current, minimum)
            }

          inadequatePermissionsFuture.flatMap { inadequatePermissions =>
            if (inadequatePermissions.isEmpty)
              action(request)
            else {
              Logger.warn(s"ACCESS CONTROL: [$authToken] tried to access ${request.method} ${request.path}, but is missing permissions: $inadequatePermissions")
              val failureResponses = inadequatePermissions.map {
                case InadequatePermissions(permType, current, minimum) => AccessControlFailureResponse(permType, current, minimum)
              }
              Future.successful(Forbidden(Json.toJson(failureResponses.map(Json.toJson(_)))).as("application/json"))
            }
          }.recoverWith {
            case (e: AppNotFoundException) =>
              Logger.info("ACCESS CONTROL: No app was found with the given auth token")
              Future.successful(NotFound(Json.toJson(GenericResponse(success = false, None, Some(Seq("No app was found with that auth token. Please provide a valid auth token with the 'Auth-Token' http header"))))).as("application/json"))
          }
      }
    }

    lazy val parser = action.parser
  }
}
