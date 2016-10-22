package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class PayloadStation(source: SourceData) extends SafeParsed[PayloadStation.type] {
  val clsid       = parse(schema.clsid)
  val isContainer = parse(schema.isContainer)
  val count       = parse(schema.count)
  val typ         = parse(schema.typ)

  def isEmpty: Boolean = count <= 0
  def nonEmpty: Boolean = !isEmpty
}

object PayloadStation extends Schema[PayloadStation] {
  val clsid       = required[String]("CLSID")
  val isContainer = required[Boolean]("container")
  val count       = required[Int]("count")
  val typ         = required[UnitType]("weapon")
}

