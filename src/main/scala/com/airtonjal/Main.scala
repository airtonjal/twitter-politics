package com.airtonjal

import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.hadoop.cfg.ConfigurationOptions

import com.typesafe.scalalogging.Logger

import scala.io.Source

/**
 * Application entry point
 * @author <a href="mailto:airtonjal@gmail.com">Airton Libório</a>
 */
object Main {

  private val log = Logger(getClass())

  def main(args: Array[String]) {
    if (args.length < 6) {
      log.error("Twitter politics usage: <master> <key> <secret key> <access token> <access token secret> <terms file> <es-resource> [es-nodes]")
      System.exit(1)
    }

    val Array(master, consumerKey, consumerSecret, accessToken, accessTokenSecret, termsFile, esResource) = args.take(7)
    val esNodes = args.length match {
      case x: Int if x > 7 => args(7)
      case _ => "localhost"
    }

//    val terms = Source.fromURL("file://" + termsFile).mkString
    val terms = Source.fromURL("file://" + termsFile).mkString.split("\n")
    terms.foreach(t => log.info("Filtering term: " + t))

    setupTwitter(consumerKey, consumerSecret, accessToken, accessTokenSecret)
    val ssc = new StreamingContext(master, "Twitter politics stream", Seconds(2))

    // TODO: change this to a list since the terms are not being counted by now
//    val terms = Seq(("obama", 0),("republicans", 0),("democrats", 0),("elections", 0),("clinton", 0),
//      ("ted cruz", 0),("jeb bush", 0),("ben carson", 0), ("@SenTedCruz", 0))
//    var distTerms = ssc.sparkContext.parallelize(terms)

    val stream = TwitterUtils.createStream(ssc, None, terms)
//    stream.print()

    val hashTags = stream.flatMap(status => status.getHashtagEntities.map("#" + _.getText))

    val WINDOW = 120
    // Print popular hashtags in the last 120 seconds
    val topHashTags = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(WINDOW))
      .map{ case (hashTag, count) => (count, hashTag) }
      .transform(_.sortByKey(false))
    topHashTags.foreachRDD(tweetRDD => {
      val topList = tweetRDD.take(10)
      log.info("-----------------------------------------------------------")
      log.info("Popular topics in last " + WINDOW + " seconds (%s total):".format(tweetRDD.count()))
      topList.foreach{case (count, tag) => log.info("%s (%s tweets)".format(tag, count))}
      log.info("-----------------------------------------------------------")
    })

    ssc.sparkContext.setLocalProperty("spark.serializer", classOf[KryoSerializer].getName)

    import org.elasticsearch.spark._
    stream.foreachRDD((tweetRDD, time) => {
      tweetRDD.map{ t =>
        val location = t.getGeoLocation match {
          case null => None
          case gl   => Some(Map("lat" -> t.getGeoLocation.getLatitude, "lon" -> t.getGeoLocation.getLongitude))
        }
        Map("text"     -> t.getText,
          "sentiment"  -> SimpleSentimentAnalysis.classify(t.getText)._1,
          "created_at" -> t.getCreatedAt,
          "location"   -> location,
          "language"   -> t.getLang,
          "user"       -> t.getUser.getName)
          .filter(kv => kv._2 != null && kv._2 != None)
      }.saveToEs(esResource, Map(ConfigurationOptions.ES_NODES -> esNodes))
    })

    log.info("Starting twitter-politics stream")

    ssc.start()
    ssc.awaitTermination()

    log.info("Finishing twitter-politics stream")
  }

  def setupTwitter(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String) ={
    // Set up the system properties for twitter
    System.setProperty("twitter4j.oauth.consumerKey", consumerKey)
    System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)
    // https:  all kinds of fun
    System.setProperty("twitter4j.restBaseURL", "https://api.twitter.com/1.1/")
    System.setProperty("twitter4j.streamBaseURL", "https://stream.twitter.com/1.1/")
    System.setProperty("twitter4j.siteStreamBaseURL", "https://sitestream.twitter.com/1.1/")
    System.setProperty("twitter4j.userStreamBaseURL", "https://userstream.twitter.com/1.1/")
    System.setProperty("twitter4j.oauth.requestTokenURL", "https://api.twitter.com/oauth/request_token")
    System.setProperty("twitter4j.oauth.accessTokenURL", "https://api.twitter.com/oauth/access_token")
    System.setProperty("twitter4j.oauth.authorizationURL", "https://api.twitter.com/oauth/authorize")
    System.setProperty("twitter4j.oauth.authenticationURL", "https://api.twitter.com/oauth/authenticate")
  }


}
