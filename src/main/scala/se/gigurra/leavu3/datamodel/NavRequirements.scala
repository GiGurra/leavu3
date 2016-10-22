package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Parsed, Schema}

import scala.language.implicitConversions

case class NavRequirements(source: SourceData = Map.empty) extends SafeParsed[NavRequirements.type] {
  val altitude      = parse(schema.altitude)
  val verticalSpeed = parse(schema.verticalSpeed)
  val roll          = parse(schema.roll).toDegrees
  val pitch         = parse(schema.pitch).toDegrees
  val speed         = parse(schema.speed)
}

object NavRequirements extends Schema[NavRequirements] {
  val altitude      = required[Float]("altitude", default = 0)
  val verticalSpeed = required[Float]("vertical_speed", default = 0)
  val roll          = required[Float]("roll", default = 0)
  val pitch         = required[Float]("pitch", default = 0)
  val speed         = required[Float]("speed", default = 0)
}

