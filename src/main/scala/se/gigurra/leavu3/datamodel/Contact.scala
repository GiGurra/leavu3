package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.util.{CurTime, Memorized}

object SensorFlags {
  val RADAR_VIEW   = 0x0002
  val EOS_VIEW     = 0x0004
  val RADAR_BUG    = 0x0008
  val EOS_BUG      = 0x0010
  val RADAR_TWS    = 0x0020
  val EOS_TWS      = 0x0040
  val HUMAN_PLANE  = 0x0200
  val EASY_LOCK    = 0x0400
  val RADAR_HOJ    = 0x0800
}

case class RadarFlags(flags: Int) {
  def hasAnyOf(flag: Int): Boolean = {
    (flags & flag) != 0
  }
  def hasAllOf(flag: Int): Boolean = {
    (flags & flag) == flag
  }
  def hasNoneOf(flag: Int): Boolean = {
    (flags & flag) == 0
  }
}

case class Contact(source: SourceData) extends SafeParsed[Contact.type] {
  val id                     = parse(schema.id)
  val course                 = parse(schema.course).toDegrees
  val flags                  = RadarFlags(parse(schema.flags))
  val aspect                 = parse(schema.aspect).toDegrees
  val verticalViewingAngle   = parse(schema.verticalViewingAngle).toDegrees
  val horizontalViewingAngle = parse(schema.horizontalViewingAngle).toDegrees
  val updatesNumber          = parse(schema.updatesNumber)
  val startOfLock            = parse(schema.startOfLock)
  val rcs                    = parse(schema.rcs)
  val forces                 = parse(schema.forces)
  val country                = parse(schema.country)
  val burnthrough            = parse(schema.burnthrough)
  val jamming                = parse(schema.jamming) != 0
  val closure                = parse(schema.closure)
  val machNumber             = parse(schema.machNumber)
  val spatial                = parse(schema.spatial)
  val typ                    = parse(schema.typ)
  val velocity               = parse(schema.velocity)
  val distance               = parse(schema.distance)


  val timestamp              = parse(schema.timestamp)
  def ageSeconds             = CurTime.seconds - timestamp

  def position               = spatial.position
  def pitch                  = spatial.pitch
  def roll                   = spatial.roll
  def heading                = spatial.heading

  import SensorFlags._
  def isDesignated           = flags.hasAnyOf(RADAR_BUG | EOS_BUG | RADAR_HOJ)
  def isEcmStrobe            = jamming && !burnthrough
  def isRws                  = flags.hasAnyOf(RADAR_VIEW | EOS_VIEW)

  def haveIff: Boolean       = !isEcmStrobe && flags.hasAnyOf(RADAR_BUG | RADAR_VIEW | RADAR_TWS)

}

object Contact extends Schema[Contact] {
  val id                     = required[Int]("ID")
  val course                 = required[Float]("course")
  val flags                  = required[Int]("flags")
  val aspect                 = required[Float]("delta_psi")
  val verticalViewingAngle   = required[Float]("fin")
  val horizontalViewingAngle = required[Float]("fim")
  val updatesNumber          = required[Int]("updates_number")
  val startOfLock            = required[Double]("start_of_lock")
  val rcs                    = required[Double]("reflection")
  val forces                 = required[Vec3]("forces")
  val country                = required[Int]("country")
  val burnthrough            = required[Boolean]("jammer_burned")
  val jamming                = required[Int]("isjamming")
  val closure                = required[Float]("convergence_velocity")
  val machNumber             = required[Float]("mach")
  val spatial                = required[Spatial]("position")
  val typ                    = required[UnitType]("type")
  val velocity               = required[Vec3]("velocity")
  val distance               = required[Float]("distance")
  val timestamp              = required[Double]("timestamp", default = CurTime.seconds)


  case class FromOwnRadar(order: scala.collection.Map[Int, Int]) {
    implicit class RichContact(c: Contact) {
      def index: Int = order.getOrElse(c.id, -1)
      def memorized[T](default: => T, f: Memorized[Contact] => T): T = if (!c.isRws || c.isDesignated) GameIn.rdrPositionMemory(c).fold(default)(f) else GameIn.rdrMemory(c).fold(default)(f)
      def news: Double = memorized(1.0, _.news)
      def age: Double = memorized(0.0, _.age)
      def visible: Boolean = !hidden
      def hidden: Boolean = memorized(false, _.hidden)
      def lag: Double = if (c.isRws) 0.0 else age
    }
  }

}
