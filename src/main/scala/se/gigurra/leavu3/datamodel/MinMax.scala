package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class MinMax(source: SourceData = Map.empty) extends SafeParsed[MinMax.type] {
  val min = parse(schema.min)
  val max = parse(schema.max)
}

object MinMax extends Schema[MinMax] {
  val min = required[Float]("min", default = 0)
  val max = required[Float]("max", default = 0)
}

