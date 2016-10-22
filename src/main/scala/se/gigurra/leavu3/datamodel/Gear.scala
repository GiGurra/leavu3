package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Gear(source: SourceData = Map.empty) extends SafeParsed[Gear.type] {
  val value  = parse(schema.value)
  val nose   = parse(schema.nose)
  val main   = parse(schema.main)
  val status = parse(schema.status)
}

object Gear extends Schema[Gear] {
  val value  = required[Float]("value", default = 0)
  val nose   = required[Wheel]("nose", default = Wheel())
  val main   = required[BackWheels]("main", default = BackWheels())
  val status = required[Int]("status", default = 0)
}