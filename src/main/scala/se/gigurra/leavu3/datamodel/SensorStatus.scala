package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class SensorsStatus(source: SourceData = Map.empty) extends SafeParsed[SensorsStatus.type] {
  val manufacturer     = parse(schema.manufacturer)
  val launchAuthorized = parse(schema.launchAuthorized)
  val eosOn            = parse(schema.eosOn)
  val radarOn          = parse(schema.radarOn)
  val laserOn          = parse(schema.laserOn)
  val ecmOn            = parse(schema.ecmOn)
  val scale            = parse(schema.scale)
  val tdc              = parse(schema.tdc)
  val scanZone         = parse(schema.scanZone)
  val prf              = parse(schema.prf)

  def sensorOn: Boolean = radarOn || eosOn
  def sensorOff: Boolean = !sensorOn

  def tdcBra(ownHeading: Double): Option[Bra] = {
    if (sensorOn) {
      val dist = math.max(1, (tdc.y + 1.0) * scale.distance * 0.5) // Place the tdc at least 1 meter out... so we get a decent position on the bscope
      val halfWidth = scale.azimuth / 2.0
      val bearing = ownHeading + halfWidth * tdc.x
      Some(Bra(bearing, dist, 0.0))
    } else {
      None
    }
  }

  def tdcPosition(ownHeading: Double, ownPosition: Vec2): Option[Vec2] = {
    tdcBra(ownHeading).map(_.toOffset + ownPosition)
  }
}

object SensorsStatus extends Schema[SensorsStatus] {
  val manufacturer      = required[String]("Manufacturer", default = "")
  val launchAuthorized  = required[Boolean]("LaunchAuthorized", default = false)
  val eosOn             = required[Boolean]("optical_system_on", default = false)
  val radarOn           = required[Boolean]("radar_on", default = false)
  val laserOn           = required[Boolean]("laser_on", default = false)
  val ecmOn             = required[Boolean]("ECM_on", default = false)
  val scale             = required[RadarDisplayScale]("scale", default = RadarDisplayScale())
  val tdc               = required[TdcPosition]("TDC", default = TdcPosition())
  val scanZone          = required[ScanZone]("ScanZone", default = ScanZone())
  val prf               = required[Prf]("PRF", default = Prf())
}
