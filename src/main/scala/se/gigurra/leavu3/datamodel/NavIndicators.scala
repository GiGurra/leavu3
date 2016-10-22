package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class NavIndicators(source: SourceData = Map.empty) extends SafeParsed[NavIndicators.type] {
  val requirements = parse(schema.requirements)
  val acs          = parse(schema.acs)
  val mode         = parse(schema.mode)
}

object NavIndicators extends Schema[NavIndicators] {
  val requirements = required[NavRequirements]("Requirements", default = NavRequirements())
  val acs          = required[Acs]("ACS", default = Acs())
  val mode         = required[AircraftMode]("SystemMode", default = AircraftMode())
}

