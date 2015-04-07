package com.airtonjal

import org.apache.commons.logging.LogFactory

import scala.io.Source

/**
 * A very rudimentary sentiment analysis algorithm. For each text, counts the number of negative and positive words and
 * outputs a factor indicating the tendency
 * @author <a href="mailto:airton.liborio@webradar.com">Airton Libório</a>
 */
object SimpleSentimetAnalysis {

  private val log = LogFactory.getLog(getClass())

  private val POSITIVE_FILE = "positive-words.txt"
  private val NEGATIVE_FILE = "negative-words.txt"

  log.info("Attempting to read " + POSITIVE_FILE + " as stream from resources directory")
  val positive = Source.fromURL(Source.getClass().getResource("/" + POSITIVE_FILE))("UTF-8").mkString
  val positiveSet = positive.split("\n").filter(w => !w.startsWith(";")).toSet

  log.info("Attempting to read " + NEGATIVE_FILE + " as stream from resources directory")
  val negative = Source.fromURL(Source.getClass().getResource("/" + NEGATIVE_FILE))("UTF-8").mkString
  val negativeSet = negative.split("\n").filter(w => !w.startsWith(";")).toSet

  private val POSITIVE_WORD = "positive"
  private val NEGATIVE_WORD = "negative"
  private val NEUTRAL_WORD = "neutral"
  def classify(tweet: String): (String, Int) = {
    var factor = 0
    tweet.split(" ").foreach{w =>
      if (positiveSet(w)) factor = factor + 1
      if (negativeSet(w)) factor = factor - 1
    }

    (if      (factor < 0) NEGATIVE_WORD
     else if (factor > 0) POSITIVE_WORD
     else                 NEUTRAL_WORD, factor)
  }

}
