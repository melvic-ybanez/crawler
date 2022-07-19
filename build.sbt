import Dependencies._

// give the user a nice default project!
ThisBuild / organization := "com.melvic"
ThisBuild / version := "0.1-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(BuildHelper.stdSettings)
  .settings(
    name := "crawler",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= Seq(`zio-test`, `zio-test-sbt`, `zio-http`, `zio-http-test`) ++ crawlerDependencies,
  )
  .settings(
    Docker / version          := version.value,
    Compile / run / mainClass := Option("com.melvic.crawler.Crawler"),
  )

addCommandAlias("fmt", "scalafmt; Test / scalafmt; sFix;")
addCommandAlias("fmtCheck", "scalafmtCheck; Test / scalafmtCheck; sFixCheck")
addCommandAlias("sFix", "scalafix OrganizeImports; Test / scalafix OrganizeImports")
addCommandAlias("sFixCheck", "scalafix --check OrganizeImports; Test / scalafix --check OrganizeImports")
