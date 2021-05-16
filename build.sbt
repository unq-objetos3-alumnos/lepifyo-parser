name := "lepifyo-parser"
version := "0.4"
scalaVersion := "2.13.3"
organization := "unq-objetos3-alumnos"

githubOwner := "unq-objetos3-alumnos"
githubRepository := "lepifyo-parser"
githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource.GitConfig("github.token")

libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
