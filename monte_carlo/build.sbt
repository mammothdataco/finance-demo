import AssemblyKeys._

name := "Liquity Risk Monte Carlo Simulation"
version := "0.1.0"
organization := "com.mammothdata"
scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
	"org.apache.spark" % "spark-core_2.10" % "1.2.0" % "provided",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)

assemblySettings

jarName in assembly := "liquidity-monte-carlo.jar"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

