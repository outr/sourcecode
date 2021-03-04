// Scala versions
val scala213 = "2.13.5"
val scala212 = "2.12.13"
val scala211 = "2.11.12"
val scala3 = "3.0.0-RC1"
val scala2 = List(scala213, scala212, scala211)
val allScalaVersions = scala3 :: scala2
val scalaJVMVersions = allScalaVersions
val scalaJSVersions = allScalaVersions
val scalaNativeVersions = scala2

name := "sourcecode"
organization in ThisBuild := "com.outr"
version in ThisBuild := "0.2.4"
scalaVersion in ThisBuild := scala213
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
javacOptions in ThisBuild ++= Seq("-source", "1.8", "-target", "1.8")

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/sourcecode/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "sourcecode", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/sourcecode"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/sourcecode"),
    "scm:git@github.com:outr/sourcecode.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)

testOptions in ThisBuild += Tests.Argument("-oD")

// Dependency versions
val collectionCompatVersion: String = "2.4.2"
val testyVersion: String = "1.0.0"

// set source map paths from local directories to github path
val sourceMapSettings = List(
  scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/outr/sourcecode/$headCommit/"
    s"-P:scalajs:mapSourceURI:$local->$remote"
  }
)

lazy val root = project.in(file("."))
  .aggregate(
    sourcecode.js, sourcecode.jvm, sourcecode.native
  )
  .settings(
    name := "sourcecode",
    publish := {},
    publishLocal := {}
  )

lazy val sourcecode = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "sourcecode",
    libraryDependencies ++= Seq(
      "com.outr" %%% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= (
      if (isDotty.value) {
        Nil
      } else {
        Seq(
          "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion,
          "org.scala-lang" % "scala-reflect" % scalaVersion.value,
          "org.scala-lang" % "scala-compiler" % scalaVersion.value
        )
      }
    ),
    Compile / unmanagedSourceDirectories ++= {
      val major = if (isDotty.value) "-3" else "-2"
      List(CrossType.Pure, CrossType.Full).flatMap(
        _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
      )
    }
  )
  .jsSettings(
    crossScalaVersions := scalaJSVersions,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions
  )
  .nativeSettings(
    scalaVersion := scala213,
    crossScalaVersions := scalaNativeVersions
  )
