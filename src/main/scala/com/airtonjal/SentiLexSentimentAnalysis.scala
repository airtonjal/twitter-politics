package com.airtonjal

import com.typesafe.scalalogging.Logger

import scala.io.Source

/**
  * A rudimentary sentiment analysis implementation based on <a href="http://xldb.fc.ul.pt/wiki/SentiLex-PT01">SentiLex</a>
  * @author <a href="mailto:airtonjal@gmail.com">Airton Libório</a>
  */
object SentiLexSentimentAnalysis {

  private val log = Logger(getClass)

  private val POSITIVE_LABEL = "POL=1"
  private val NEGATIVE_LABEL = "POL=-1"
  private val FLEX_FILE = "SentiLex-flex-PT01.txt"
  private val LEM_FILE  = "SentiLex-lem-PT01.txt"

  log.info("Attempting to read " + FLEX_FILE + " as stream from resources directory")
  private val flex = Source.fromInputStream(getClass.getResourceAsStream("/SentiLex-PT01/" + FLEX_FILE))("UTF-8")
    .mkString.split("\n")
  private val lem  = Source.fromInputStream(getClass.getResourceAsStream("/SentiLex-PT01/" + LEM_FILE))("UTF-8")
    .mkString.split("\n")

  private val positiveSet = toPositiveSet(flex).union(toPositiveSet(lem))
  private val negativeSet = toNegativeSet(flex).union(toNegativeSet(lem))

  private val POSITIVE_WORD = "positive"
  private val NEGATIVE_WORD = "negative"
  private val NEUTRAL_WORD = "neutral"

  def classify(tweet: String): (String, Int) = {
    var factor = 0
    tweet.split(" ").foreach{w =>
      if (positiveSet(w)) factor = factor + 1
      if (negativeSet(w)) factor = factor - 1
    }

    (if     (factor < 0) NEGATIVE_WORD
    else if (factor > 0) POSITIVE_WORD
    else                 NEUTRAL_WORD, factor)
  }

  private def toPositiveSet(words: Array[String]) =
    words.filter(_.contains(POSITIVE_LABEL)).map(_.split(",")(0)).toSet

  private def toNegativeSet(words: Array[String]) =
    words.filter(_.contains(NEGATIVE_LABEL)).map(_.split(",")(0)).toSet

}
