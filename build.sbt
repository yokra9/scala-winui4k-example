val scala3Version = "3.9.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-winui4k-example",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    fork := true,

    libraryDependencies ++= Seq(
      "com.appkitbox.winui4k" % "winui4k-all" % "0.1.0"
    )
  )
