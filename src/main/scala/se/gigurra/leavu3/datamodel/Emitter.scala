package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema

case class Emitter(source: SourceData) extends SafeParsed[Emitter.type] {
  val id          = parse(schema.id)
  val signalType  = parse(schema.signalType)
  val azimuth     = parse(schema.azimuth).toDegrees
  val priority    = parse(schema.priority)
  val power       = parse(schema.power)
  val typ         = parse(schema.unitType)

  val level: LockLevel = signalType match {
    case Emitter.RADAR_SEARCH   => LockLevel.Search
    case Emitter.RADAR_TWS      => LockLevel.Search
    case Emitter.RADAR_LOCK     => LockLevel.Lock
    case Emitter.MISSILE_LAUNCH => LockLevel.Launch
    case Emitter.MISSILE_ACTIVE => LockLevel.Missile
    case _                      => LockLevel.Unknown
  }

  val isSearch = level < LockLevel.Lock
  val isLock = level >= LockLevel.Lock

}

object Emitter extends Schema[Emitter] {
  val id          = required[Int]("ID")
  val signalType  = required[String]("SignalType")
  val azimuth     = required[Float]("Azimuth")
  val priority    = required[Float]("Priority")
  val power       = required[Float]("Power")
  val unitType    = required[UnitType]("Type")

  val RADAR_SEARCH = "scan"
  val RADAR_TWS = "track_while_scan"
  val RADAR_LOCK = "lock"
  val MISSILE_LAUNCH = "missile_radio_guided"
  val MISSILE_ACTIVE = "missile_active_homing"
}

sealed abstract class LockLevel(val level: Int) extends Ordered[LockLevel] {
  def compare(other: LockLevel): Int = implicitly[Ordering[Int]].compare(this.level, other.level)
}

object LockLevel {
  case object Naked extends LockLevel(0)
  case object Search extends LockLevel(1)
  case object Lock extends LockLevel(2)
  case object Launch extends LockLevel(3)
  case object Missile extends LockLevel(4)
  case object Unknown extends LockLevel(5)
  case object Ignored extends LockLevel(6)
}
