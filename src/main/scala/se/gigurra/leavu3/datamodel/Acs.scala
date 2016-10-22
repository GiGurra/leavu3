package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Acs(source: SourceData = Map.empty) extends SafeParsed[Acs.type] {
  val autoThrust = parse(schema.autoThrust)
  val mode       = parse(schema.mode)
}

object Acs extends Schema[Acs] {
  val autoThrust = required[Boolean]("autothrust", default = false)
  val mode       = required[String]("mode", default = "UKN")
}
