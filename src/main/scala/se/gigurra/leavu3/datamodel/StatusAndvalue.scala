package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class StatusAndValue(source: SourceData = Map.empty) extends SafeParsed[StatusAndValue.type] {
  val status = parse(schema.status)
  val value  = parse(schema.value)
}

object StatusAndValue extends Schema[StatusAndValue] {
  val status = required[Int]("status", default = 0)
  val value  = required[Double]("value", default = 0)
}

