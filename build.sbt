name := """elophant"""

version := "3.2.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  evolutions,
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc4",
  "com.typesafe.play" %% "play-slick" % "1.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.0",
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
)

testOptions in Test += Tests.Argument("-oDF")

javaOptions in Test += "-Dconfig.file=conf/application-test.conf"

//coverageMinimum := 80

//coverageFailOnMinimum := true

coverageExcludedPackages := "<empty>;(JavaScriptReverseRoutes)\\.scala"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
