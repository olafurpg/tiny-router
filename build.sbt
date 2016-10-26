organization in ThisBuild := "com.geirsson"
lazy val `tiny-router-root` = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(
    `tiny-routerJVM`,
    `tiny-routerJS`
  )

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  licenses := Seq(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/olafurpg/tiny-router")),
  autoAPIMappings := true,
  apiURL := Some(url("https://github.com/olafurpg/tiny-router")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/olafurpg/tiny-router"),
      "scm:git:git@github.com:olafurpg/tiny-router.git"
    )
  ),
  pomExtra :=
    <developers>
      <developer>
        <id>olafurpg</id>
        <name>Ólafur Páll Geirsson</name>
        <url>https://geirsson.com</url>
      </developer>
    </developers>
)

lazy val `tiny-router` = crossProject
  .crossType(CrossType.Full)
  .settings(
    publishSettings,
    moduleName := "tiny-router",
    version := "0.1.0",
    libraryDependencies ++= {
      if (scalaVersion.value.startsWith("2.10."))
        Seq(
          compilerPlugin(
            "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
      else
        Seq()
    },
    libraryDependencies += "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % "1.1.1" % "test"
  )
lazy val `tiny-routerJS` = `tiny-router`.js
lazy val `tiny-routerJVM` = `tiny-router`.jvm

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)
