package util

import types.{Rating, Score}

trait EloCalculator {
  val curveScalingFactor: Int
  val kFactor: Int

  def getExpectedScore(ratingA: Rating, ratingB: Rating): Score
  def getDeltaRating(ratingA: Rating, ratingB: Rating, score: Score): Rating
}

object StandardEloCalculator extends EloCalculator {
  val curveScalingFactor: Int = 400
  val kFactor: Int = 16

  def getExpectedScore(ratingA: Rating, ratingB: Rating): Score = 1 / (1 + Math.pow(10, (ratingB - ratingA)/curveScalingFactor))

  def getDeltaRating(ratingA: Rating, ratingB: Rating, score: Score): Rating = Math.round(kFactor * (score - getExpectedScore(ratingA, ratingB))).toInt
}
