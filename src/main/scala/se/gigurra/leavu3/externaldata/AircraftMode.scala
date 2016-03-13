package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class AircraftMode(source: SourceData = Map.empty) extends Parsed[AircraftMode.type] {
  val submode = parse(schema.submode)
  val master  = parse(schema.master)

  def isInCac: Boolean = master == "CAC"
}

object AircraftMode extends Schema[AircraftMode] {
  val submode = required[String]("submode", default = "NAV")
  val master  = required[String]("master", default = "NAV")
}

