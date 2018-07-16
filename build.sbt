name := "twitter-politics"
version := "0.1"
scalaVersion := "2.11.12"

val elasticsearchVersion = "6.3.1"
val sparkVersion = "2.2.1"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.elasticsearch" %  "elasticsearch"          % elasticsearchVersion,
  "org.elasticsearch" %% "elasticsearch-spark-20" % elasticsearchVersion
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core"              % sparkVersion % Provided,
  "org.apache.spark" %% "spark-streaming"         % sparkVersion % Provided,
  "org.apache.bahir" %% "spark-streaming-twitter" % sparkVersion % Provided
).map(_.exclude("org.slf4j", "slf4j-log4j12"))

mainClass in (Compile, run) := Some("com.airtonjal.Main")

updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true) // For faster builds
