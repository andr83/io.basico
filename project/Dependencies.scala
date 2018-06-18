import sbt._

object Version {
  val h2 = "1.4.195"
  val reactiveStreams = "1.0.2"
  val scalaTest = "3.0.4"
}

object Library {
  val h2 = "com.h2database" % "h2" % Version.h2
  val reactiveStreams = "org.reactivestreams" % "reactive-streams" % Version.reactiveStreams
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
}
