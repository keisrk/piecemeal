enablePlugins(ScalaJSPlugin)

name := "Piecemeal"

scalaVersion := "2.12.6"
scalaJSUseMainModuleInitializer := true

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.6"
libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.7"
libraryDependencies +="io.udash" %%% "udash-core-frontend" % "0.7.1"
libraryDependencies += "io.udash" %%% "udash-bootstrap" % "0.7.1"
libraryDependencies += "io.udash" %%% "udash-jquery" % "1.2.0"

jsDependencies += "org.webjars.npm" % "twgl.js" % "4.4.0" / "twgl-full.js"

jsDependencies += "org.webjars" % "bootstrap" % "3.3.7-1" / "bootstrap.js" minified "bootstrap.min.js" dependsOn "jquery.js"
