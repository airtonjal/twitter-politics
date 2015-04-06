package com.airtonjal

import org.apache.commons.logging.LogFactory
import org.apache.spark.SparkContext
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

  private val log = LogFactory.getLog(getClass())

  def main(args: Array[String]) {
    if (args.length < 5) {
      log.fatal("Twitter politics usage <master> <key> <secret key> <access token> <access token secret> <es-resource> [es-nodes]")
      System.exit(1)
    }

    val Array(master, consumerKey, consumerSecret, accessToken, accessTokenSecret, esResource) = args.take(6)
    val esNodes = args.length match {
      case x: Int if x > 6 => args(6)
      case _ => "localhost"
    }

    setupTwitter(consumerKey, consumerSecret, accessToken, accessTokenSecret)
    val ssc = new StreamingContext(master, "Twitter politics stream", Seconds(2))
    val stream = TwitterUtils.createStream(ssc, None,
      Seq("obama","republicans","democrats","elections","clinton","ted cruz","jeb bush","ben carson"))

//    stream.print()

//    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))
    val hashTags = stream.flatMap(status => status.getHashtagEntities.map("#" + _.getText))

    val top60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(60))
      .map{ case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

    // Print popular hashtags in the last 60 seconds
    top60.foreachRDD(rdd => {
      val topList = rdd.take(10)
      log.info("Popular topics in last 60 seconds (%s total):".format(rdd.count()))
      log.info("-----------------------------------------------------------")
      topList.foreach{case (count, tag) => log.info("%s (%s tweets)".format(tag, count))}
      log.info("-----------------------------------------------------------")
    })

//    stream.foreachRDD{(tweetRDD, time) =>
//      val tweet = tweetRDD.take(1)
//      tweet.foreach(t => print(t.getText))
//    }

    ssc.start()
    ssc.awaitTermination()



    // Old way
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
