package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class StatusFlags(source: SourceData = Map.empty) extends SafeParsed[StatusFlags.type] {
  val aiOn        = parse(schema.aiOn)
  val jamming     = parse(schema.jamming)
  val born        = parse(schema.born)
  val static      = parse(schema.static)
  val human       = parse(schema.human)
  val radarActive = parse(schema.radarActive)
  val iRJamming   = parse(schema.iRJamming)
  val invisible   = parse(schema.invisible)
}

object StatusFlags extends Schema[StatusFlags] {
  val aiOn        = required[Boolean]("AI_ON", default = false)
  val jamming     = required[Boolean]("Jamming", default = false)
  val born        = required[Boolean]("Born", default = false)
  val static      = required[Boolean]("Static", default = false)
  val human       = required[Boolean]("Human", default = false)
  val radarActive = required[Boolean]("RadarActive", default = false)
  val iRJamming   = required[Boolean]("IRJamming", default = false)
  val invisible   = required[Boolean]("Invisible", default = false)
}
