import scala.sys.process._
import java.net.URI
import java.io.File

val scala3Version = "3.9.0"
val windowsAppSdkVersion = "2.4.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-winui4k-example",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    fork := true,
    Compile / run / javaOptions ++= Seq("--enable-native-access=ALL-UNNAMED"),

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
      "com.appkitbox.winui4k" % "winui4k-all" % "0.1.0",
      "org.scalameta" %% "munit" % "1.3.6" % Test
    ),

    testOptions += Tests.Argument(TestFrameworks.MUnit, "+junitxml")
  )

commands += Command.command("downloadInstallers") { state =>
  val version = windowsAppSdkVersion
  val base = Project.extract(state).get(baseDirectory)
  val log = state.log
  val dir = base
  dir.mkdirs()
  for (arch <- Seq("x86", "x64", "arm64")) {
    val fileName = s"WindowsAppRuntimeInstall-$arch.exe"
    val dest = dir / fileName
    if (dest.exists()) {
      log.info(s"Already exists: $dest")
    } else {
      val url =
        s"https://aka.ms/windowsappsdk/2.4/$version/windowsappruntimeinstall-$arch.exe"
      log.info(s"Downloading $fileName ...")
      val conn = URI(url).toURL().openConnection()
      conn.setRequestProperty("User-Agent", "curl/8.0")
      val input = conn.getInputStream()
      try {
        java.nio.file.Files.copy(
          input,
          dest.toPath(),
          java.nio.file.StandardCopyOption.REPLACE_EXISTING
        )
      } finally {
        input.close()
      }
      log.info(s"Downloaded: $dest")
    }
  }
  state
}
