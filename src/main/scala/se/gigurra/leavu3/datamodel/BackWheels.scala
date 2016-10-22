package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class BackWheels(source: SourceData = Map.empty) extends SafeParsed[BackWheels.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
}

object BackWheels extends Schema[BackWheels] {
  val left  = required[Wheel]("left", default = Wheel())
  val right = required[Wheel]("right", default = Wheel())
}


