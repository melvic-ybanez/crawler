import sbt._

object Dependencies {
  val ZioVersion   = "1.0.13"
  val ZHTTPVersion = "1.0.0.0-RC24"

  val `zio-http`      = "io.d11" %% "zhttp" % ZHTTPVersion
  val `zio-http-test` = "io.d11" %% "zhttp" % ZHTTPVersion % Test

  val `zio-test`     = "dev.zio" %% "zio-test"     % ZioVersion % Test
  val `zio-test-sbt` = "dev.zio" %% "zio-test-sbt" % ZioVersion % Test

  val circeVersion = "0.14.1"
  val circeDependencies = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

  val jsoupDeps = "org.jsoup" % "jsoup" % "1.15.2"
}
