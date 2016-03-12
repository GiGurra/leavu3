package se.gigurra.leavu3.externaldata

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class BeaconIndicators(source: SourceData = Map.empty) extends Parsed[BeaconIndicators.type] {
  val glideSlope  = parse(schema.glideslope)
  val localizer   = parse(schema.localizer)
  val outerMarker = parse(schema.outerMarker)
  val innerMarker = parse(schema.innerMarker)
}

object BeaconIndicators extends Schema[BeaconIndicators] {
  val glideslope  = required[Boolean]("glideslope_deviation_beacon_lock", default = false)
  val localizer   = required[Boolean]("course_deviation_beacon_lock", default = false)
  val outerMarker = required[Boolean]("airfield_far", default = false)
  val innerMarker = required[Boolean]("airfield_near", default = false)
}

