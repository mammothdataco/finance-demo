import AssemblyKeys._

name := "Liquity Risk Monte Carlo Streaming Simulation"
version := "0.2.0"
organization := "com.mammothdata"
scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"org.apache.spark" % "spark-core_2.10" % "1.2.0" % "provided",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.apache.spark" % "spark-streaming_2.10" % "1.2.0" % "provided",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.2.0",
  "org.json4s" %% "json4s-native" % "3.2.10"
)

assemblySettings

jarName in assembly := "liquidity-monte-carlo-streaming.jar"


assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}