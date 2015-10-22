package controllers

import play.api.mvc._
import play.api.Logger
import types.{Rating, Score}
import util.StandardEloCalculator

class Static extends Controller {
  def expectedEloChange(maybeRatingA: Option[Rating], maybeRatingB: Option[Rating], maybeScore: Option[Score]) = Action { request =>
    val result = for {
      ratingA <- maybeRatingA
      ratingB <- maybeRatingB
      score <- maybeScore
      deltaRatingA = StandardEloCalculator.getDeltaRating(ratingA, ratingB, score)
      newRatingA = ratingA + deltaRatingA
      newRatingB = ratingB - deltaRatingA
    } yield Ok("New Rating A: " + newRatingA + "   New Rating B: " + newRatingB)

    result getOrElse Ok("Missing information. Please provide ratings and final score. Eg: GET host/expectedEloChange?ratingA=1400&ratingB=1200&score=1.0")
  }
}
