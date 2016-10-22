package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class TdcPosition(source: SourceData = Map.empty) extends SafeParsed[TdcPosition.type] {
  val x = parse(schema.x)
  val y = parse(schema.y)
}

object TdcPosition extends Schema[TdcPosition] {
  val x = required[Float]("x", default = 0)
  val y = required[Float]("y", default = 0)
}

