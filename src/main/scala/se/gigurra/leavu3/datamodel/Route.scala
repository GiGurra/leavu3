package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class RouteWire(source: SourceData = Map.empty) extends SafeParsed[RouteWire.type] {
  val waypoints       = parse(schema.waypoints)
  val currentWaypoint = parse(schema.currentWaypoint)
  def toRoute: Route = Route(waypoints.map(_.toWaypoint), currentWaypoint.toWaypoint)
}

case class Route(waypoints: Seq[Waypoint] = Nil,
                 currentWaypoint: Waypoint = Waypoint()) {
  def withWaypoints(wps: Seq[Waypoint]) = copy(waypoints = wps)
}

object RouteWire extends Schema[RouteWire] {
  val waypoints       = required[Seq[WaypointWire]]("route", default = Seq.empty)
  val currentWaypoint = required[WaypointWire]("goto_point", default = WaypointWire())
}

