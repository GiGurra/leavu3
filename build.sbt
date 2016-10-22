val leavu3 = Project(id = "leavu3", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",

    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    mainClass in assembly := Some("se.gigurra.leavu3.DesktopMain"),

    libraryDependencies ++= Seq(
      "com.github.gigurra"    %%  "service-utils"         % "0.1.14-SNAPSHOT",
      "com.github.gigurra"    %%  "glasciia-desktop"      % "0.2.2-SNAPSHOT",
      "net.java.dev.jna"      %   "jna-platform"          % "4.2.2",
      "net.java.dev.jna"      %   "jna"                   % "4.2.2"
    ),

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
