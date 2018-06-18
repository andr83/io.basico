import sbt.Keys.scalaVersion
import sbt._

val scalacOpts = Seq(
  "-encoding",
  "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-language:postfixOps",
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Xcheckinit", // runtime error when a val is not initialized due to trait hierarchies (instead of NPE somewhere else)
  //"-Xfatal-warnings", // all warnings become errors
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-Xlog-implicits"
  //"-Ywarn-unused-import"
)

val commonSettings = Seq(
  organization := "io.basico",
  resolvers += Resolver.sonatypeRepo("releases"),
  scalaVersion := "2.12.4",
  scalacOptions ++= scalacOpts,
  scalacOptions in (Compile, console) --= Seq(
    "-Ywarn-unused:imports"
    //"-Xfatal-warnings"
  ),
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  publishMavenStyle := true,
  parallelExecution in Test := false
)

lazy val root = project
  .in(file("."))
  .aggregate(core, macros, jdbc)
  .settings(commonSettings)
  .settings(publishArtifact := false, skip in publish := true)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(Library.reactiveStreams))
  .dependsOn(macros)

lazy val macros = project
  .in(file("macros"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
    )
  )

lazy val jdbc = project
  .in(file("jdbc"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    Library.scalaTest,
    Library.h2 % Test
  ))
  .dependsOn(core)
