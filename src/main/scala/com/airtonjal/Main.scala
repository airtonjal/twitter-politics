package com.airtonjal

import org.apache.spark.streaming.twitter._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.immutable.HashMap
// twitter imports
import twitter4j.TwitterFactory

/**
 * Application entry point
 * @author <a href="mailto:airtonjal@gmail.com">Airton Libório</a>
 */
object Main {

  def main(args: Array[String]) {
    if (args.length < 5)
      System.err.println("Twitter politics usage <master> <key> <secret key> <access token> <access token secret> <es-resource> [es-nodes]")

    val Array(master, consumerKey, consumerSecret, accessToken, accessTokenSecret, esResource) = args.take(6)
    val esNodes = args.length match {
      case x: Int if x > 6 => args(6)
      case _ => "localhost"
    }

    setupTwitter(consumerKey, consumerSecret, accessToken, accessTokenSecret)

    val ssc = new StreamingContext(master, "IndexTweetsLive", Seconds(1))

    val tweets = TwitterUtils.createStream(ssc, None)
    tweets.print()

    // Old way
//    tweets.foreachRDD{(tweetRDD, time) =>
//
//    }
    /**
    tweets.foreachRDD{(tweetRDD, time) =>
      val sc = tweetRDD.context
      val jobConf = SharedESConfig.setupEsOnSparkContext(sc, esResource, Some(esNodes))
      val tweetsAsMap = tweetRDD.map(SharedIndex.prepareTweets)
      tweetsAsMap.saveAsHadoopDataset(jobConf)
    }
      **/
//    // New fancy way
//    tweets.foreachRDD{(tweetRDD, time) =>
//      val sc = tweetRDD.context
//      val sqlCtx = new SQLContext(sc)
//      val tweetsAsCS = sqlCtx.createSchemaRDD(tweetRDD.map(SharedIndex.prepareTweetsCaseClass))
//      tweetsAsCS.saveToEs(esResource)
//    }
//    println("pandas: sscstart")
//    ssc.start()
//    println("pandas: awaittermination")
//    ssc.awaitTermination()
//    println("pandas: done!")
  }

  def prepareTweets(tweet: twitter4j.Status) = {
    println("panda preparing tweet!")
    val fields = tweet.getGeoLocation() match {
      case null => HashMap(
        "docid" -> tweet.getId().toString,
        "message" -> tweet.getText(),
        "hashTags" -> tweet.getHashtagEntities().map(_.getText()).mkString(" ")
      )
      case loc => {
        val lat = loc.getLatitude()
        val lon = loc.getLongitude()
        HashMap(
          "docid" -> tweet.getId().toString,
          "message" -> tweet.getText(),
          "hashTags" -> tweet.getHashtagEntities().map(_.getText()).mkString(" "),
          "location" -> s"$lat,$lon"
        )
      }
    }
//    val output = mapToOutput(fields)
//    output
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

  def fetchTweets(ids: Seq[String]) = {
    val twitter = new TwitterFactory().getInstance();
  }

}
