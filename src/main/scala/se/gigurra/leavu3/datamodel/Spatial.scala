package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Spatial(source: SourceData = Map.empty) extends SafeParsed[Spatial.type] {
   val fwd     = parse(schema.x)
   val up      = parse(schema.y)
   val rwing   = parse(schema.z)
   val position = parse(schema.position)

  val pitch    = math.asin(fwd.z).toDegrees
  val roll     = math.atan2(up.x, up.z).toDegrees
  val heading  = math.atan2(fwd.x, fwd.y).toDegrees
}

object Spatial extends Schema[Spatial] {
  val x        = required[Vec3]("x", default = Vec3())
  val y        = required[Vec3]("y", default = Vec3())
  val z        = required[Vec3]("z", default = Vec3())
  val position = required[Vec3]("p", default = Vec3())
}

