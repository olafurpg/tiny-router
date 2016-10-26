lazy val `tiny-router` = crossProject
  .crossType(CrossType.Full)
  .settings(
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
