name := "twitter-politics"
version := "0.1"
scalaVersion := "2.11.12"

val log4jVersion = "2.2"
val jacksonVersion = "2.5.1"
val elasticsearchVersion = "6.3.1"
val sparkVersion = "2.3.1"

resolvers ++= Seq(
  "Apache" at "https://repository.apache.org/content/repositories/releases",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  //  "http://repo.typesafe.com/typesafe/releases",
//  "http://repo.typesafe.com/typesafe/maven-releases/",
//  "https://oss.sonatype.org/content/groups/scala-tools",
  "conjars" at  "http://conjars.org/repo"
//  "Clojure" at "http://clojars.org/repo",
//  "Twitter" at "http://maven.twttr.com",
//  "Twitter4J" at "http://twitter4j.org/maven2"
)

libraryDependencies ++= Seq(
//  "org.scala-lang" % "scala-library" % "2.11.12",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion ,
  "org.apache.spark" %% "spark-streaming-twitter" % "1.6.3",

//  "com.twitter.elephantbird" % "elephant-bird" % "4.5",
//  "com.twitter.elephantbird" % "elephant-bird-core" % "4.5",
//  "org.twitter4j" % "twitter4j-stream"  % "3.0.6",

  "org.elasticsearch" % "elasticsearch" % elasticsearchVersion,
  "org.elasticsearch" % "elasticsearch-hadoop" % elasticsearchVersion
)

mainClass in (Compile, run) := Some("com.airtonjal.Main")
