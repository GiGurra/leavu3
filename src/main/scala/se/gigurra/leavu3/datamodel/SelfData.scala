package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class SelfData(source: SourceData = Map.empty) extends SafeParsed[SelfData.type] {
  val statusFlags  = parse(schema.statusFlags)
  val callsign     = parse(schema.callsign)
  val aircraftName = parse(schema.aircraftName)
  val geoPosition  = parse(schema.geoPosition)
  val coalition    = parse(schema.coalition)
  val country      = parse(schema.country)
  val position     = parse(schema.position)
  val groupname    = parse(schema.groupname)
  val coalitionId  = parse(schema.coalitionId)
  val pitch        = parse(schema.pitch).toDegrees
  val roll         = parse(schema.roll).toDegrees
  val heading      = parse(schema.heading).toDegrees
  val typ          = parse(schema.typ)
}

object SelfData extends Schema[SelfData] {
  val statusFlags  = required[StatusFlags]("Flags", default = StatusFlags())
  val callsign     = required[String]("UnitName", default = "")
  val aircraftName = required[String]("Name", default = "")
  val geoPosition  = required[GeoPosition]("LatLongAlt", default = GeoPosition())
  val coalition    = required[String]("Coalition", default = "")
  val country      = required[Int]("Country", default = 0)
  val position     = required[Vec3]("Position", default = Vec3())
  val groupname    = required[String]("GroupName", default = "")
  val coalitionId  = required[Int]("CoalitionID", default = 0)
  val pitch        = required[Float]("Pitch", default = 0)
  val roll         = required[Float]("Bank", default = 0)
  val heading      = required[Float]("Heading", default = 0)
  val typ          = required[UnitType]("Type", default = UnitType())
}
