val ext_libgdxVersion = "1.9.2"

val leavu3 = Project(id = "leavu3", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",

    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    mainClass in assembly := Some("se.gigurra.leavu3.DesktopMain"),

    libraryDependencies ++= Seq(
      "com.github.gigurra"    %%  "service-utils"         % "0.1.13-SNAPSHOT",
      "com.badlogicgames.gdx" %   "gdx"                   % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-freetype"          % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-backend-lwjgl"     % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-platform"          % ext_libgdxVersion classifier "natives-desktop",
      "com.badlogicgames.gdx" %   "gdx-freetype-platform" % ext_libgdxVersion classifier "natives-desktop",
      "net.java.dev.jna"      %   "jna-platform"          % "4.2.2",
      "net.java.dev.jna"      %   "jna"                   % "4.2.2"
    ),

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
