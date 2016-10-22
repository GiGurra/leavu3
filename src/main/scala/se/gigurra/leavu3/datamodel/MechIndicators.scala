package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class MechIndicators(source: SourceData = Map.empty) extends SafeParsed[MechIndicators.type] {
  val wheelbrakes     = parse(schema.wheelbrakes)
  val wing            = parse(schema.wing)
  val gear            = parse(schema.gear)
  val controlSurfaces = parse(schema.controlSurfaces)
  val airintake       = parse(schema.airintake)
  val refuelingboom   = parse(schema.refuelingboom)
  val flaps           = parse(schema.flaps)
  val parachute       = parse(schema.parachute)
  val noseflap        = parse(schema.noseflap)
  val hook            = parse(schema.hook)
  val speedbrakes     = parse(schema.speedbrakes)
  val canopy          = parse(schema.canopy)
}

object MechIndicators extends Schema[MechIndicators] {
  val wheelbrakes     = required[StatusAndValue]("wheelbrakes", default = StatusAndValue())
  val wing            = required[StatusAndValue]("wing", default = StatusAndValue())
  val gear            = required[Gear]("gear", default = Gear())
  val controlSurfaces = required[ControlSurfaces]("controlsurfaces", default = ControlSurfaces())
  val airintake       = required[StatusAndValue]("airintake", default = StatusAndValue())
  val refuelingboom   = required[StatusAndValue]("refuelingboom", default = StatusAndValue())
  val flaps           = required[StatusAndValue]("flaps", default = StatusAndValue())
  val parachute       = required[StatusAndValue]("parachute", default = StatusAndValue())
  val noseflap        = required[StatusAndValue]("noseflap", default = StatusAndValue())
  val hook            = required[StatusAndValue]("hook", default = StatusAndValue())
  val speedbrakes     = required[StatusAndValue]("speedbrakes", default = StatusAndValue())
  val canopy          = required[StatusAndValue]("canopy", default = StatusAndValue())
}
