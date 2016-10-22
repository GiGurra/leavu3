package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class GameVersion(source: SourceData = Map.empty) extends SafeParsed[GameVersion.type] {
  val productName    = parse(schema.productName)
  val fileVersion    = parse(schema.fileVersion).mkString(".")
  val productVersion = parse(schema.productVersion).mkString(".")
}

object GameVersion extends Schema[GameVersion] {
  val productName    = required[String]("ProductName", default = "")
  val fileVersion    = required[Seq[Int]]("FileVersion", default = Seq.empty)
  val productVersion = required[Seq[Int]]("ProductVersion", default = Seq.empty)
}
