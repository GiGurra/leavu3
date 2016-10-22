package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class WaypointWire(source: SourceData = Map.empty) extends SafeParsed[WaypointWire.type] {
  val position      = parse(schema.position)
  val estimatedTime = parse(schema.estimatedTime)
  val speedReq      = parse(schema.speedReq)
  val pointAction   = parse(schema.pointAction)
  val index         = parse(schema.index)
  val next          = parse(schema.next)

  def toWaypoint: Waypoint = Waypoint(position, estimatedTime, speedReq, pointAction, index, next)
}

case class Waypoint(position: Vec3 = Vec3(),
                    estimatedTime: Double = 0,
                    speedReq: Double = 0,
                    pointAction: String = "",
                    index: Int = 0,
                    next: Int = 0) {
  def withIndex(i: Int) = copy(index = i)
}

object WaypointWire extends Schema[WaypointWire] {
  val position      = required[Vec3]("world_point", default = Vec3())
  val estimatedTime = required[Double]("estimated_time", default = 0)
  val speedReq      = required[Double]("speed_req", default = 0)
  val pointAction   = required[String]("point_action", default = "")
  val index         = required[Int]("this_point_num", default = 0)
  val next          = required[Int]("next_point_num", default = 0)
}

