name := "lepifyo-parser"
version := "0.4"
scalaVersion := "2.13.5"
organization := "unq-objetos3-alumnos"

githubOwner := "unq-objetos3-alumnos"
githubRepository := "lepifyo-parser"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource.GitConfig("github.token")

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.0.0"
