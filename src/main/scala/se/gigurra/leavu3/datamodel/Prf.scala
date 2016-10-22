package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Prf(source: SourceData = Map.empty) extends SafeParsed[Prf.type] {
  val selection = parse(schema.selection)
  val current   = parse(schema.current)
}

object Prf extends Schema[Prf] {
  val selection = required[String]("selection", default = "")
  val current   = required[String]("current", default = "")
}

