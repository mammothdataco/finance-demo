import AssemblyKeys._

name := "Liquity Risk Monte Carlo Streaming Simulation"
version := "0.2.0"
organization := "com.mammothdata"
scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"org.apache.spark" % "spark-core_2.10" % "1.2.0" % "provided",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "org.apache.spark" % "spark-streaming_2.10" % "1.2.0",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.2.0",
  "org.json4s" %% "json4s-native" % "3.2.10"
)

assemblySettings

jarName in assembly := "liquidity-monte-carlo-streaming.jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

