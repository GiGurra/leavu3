package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Payload(source: SourceData = Map.empty) extends SafeParsed[Payload.type] {
  val stations       = parse(schema.stations)
  val currentStation = parse(schema.currentStation)
  val cannon         = parse(schema.cannon)

  def toDlinkPayload(implicit configuration: Configuration): Map[String, DlinkPylon] = {
    val iSelected = currentStation - 1
    stations.zipWithIndex.filter(_._1.nonEmpty).map { case (station, i) =>
      (i + 1).toString -> DlinkPylon(station.typ.fullName, i == iSelected)
    }.toMap
  }
}

object Payload extends Schema[Payload] {
  val stations       = required[Seq[PayloadStation]]("Stations", default = Seq.empty)
  val currentStation = required[Int]("CurrentStation", default = 0)
  val cannon         = required[Cannon]("Cannon", default = Cannon())
}

case class DlinkPylon(source: SourceData = Map.empty) extends SafeParsed[DlinkPylon.type] {
  val name = parse(schema.name)
  val selected = parse(schema.selected)
}

object DlinkPylon extends Schema[DlinkPylon] {
  val name = required[String]("name", default = "unknown")
  val selected = required[Boolean]("selected", default = false)

  def apply(name: String, selected: Boolean): DlinkPylon = marshal(
    this.name -> name,
    this.selected -> selected
  )
}
