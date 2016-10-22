package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class AircraftMode(source: SourceData = Map.empty) extends SafeParsed[AircraftMode.type] {
  val submode = parse(schema.submode)
  val master  = parse(schema.master)

  def isInCac: Boolean = master == "CAC"
  def isStt: Boolean = submode == "STT"
}

object AircraftMode extends Schema[AircraftMode] {
  val submode = required[String]("submode", default = "NAV")
  val master  = required[String]("master", default = "NAV")
}

