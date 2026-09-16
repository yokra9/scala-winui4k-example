val scala3Version = "3.9.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-winui4k-example",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    fork := true,

    assembly / mainClass := Some("main"),
    assembly / assemblyJarName := "scala-winui4k-example-assembly.jar",
    assembly / target := baseDirectory.value,
    assembly / assemblyMergeStrategy := {
      case PathList(ps @ _*) if ps.last.endsWith("module-info.class") =>
        MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },

    libraryDependencies ++= Seq(
      "com.appkitbox.winui4k" % "winui4k-all" % "0.1.0"
    )
  )
