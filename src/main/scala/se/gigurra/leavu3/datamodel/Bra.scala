package se.gigurra.leavu3.datamodel

import se.gigurra.leavu3.lmath.{NormalizeDegrees, UnitConversions}

/**
  * Created by johan_home on 2016-03-25.
  */

case class Bra(bearingRaw: Double, range2d: Double, deltaAltitude: Double) extends UnitConversions {

  def toOffset: Vec3 = {
    val y = range2d * math.cos(bearing.toRadians)
    val x = range2d * math.sin(bearing.toRadians)
    Vec3(x, y, deltaAltitude)
  }

  def range3d: Double = {
    math.sqrt(range2d * range2d + deltaAltitude * deltaAltitude)
  }

  def elevation: Double = {
    math.atan(deltaAltitude / range2d).toDegrees
  }

  def with2dLength(d: Double): Bra = {
    val k = d / range2d
    Bra(bearingRaw, k * range2d, k * deltaAltitude)
  }

  def with3dLength(d: Double): Bra = {
    val k = d / range3d
    Bra(bearingRaw, k * range2d, k * deltaAltitude)
  }

  def withBearing(bearing: Double): Bra = {
    Bra(bearing, range2d, deltaAltitude)
  }

  def bearing: Double = NormalizeDegrees._0360(bearingRaw)

  def bearingString: String = {

    def padBearing(in: String): String = {
      in.length match {
        case 0 => "000"
        case 1 => "00" + in
        case 2 => "0" + in
        case _ => in
      }
    }

    padBearing(bearing.round.toString)
  }

  def distString(m_to_distUnit: Double): String = {
    (range2d * m_to_distUnit).round.toString
  }

  def brString(m_to_distUnit: Double): String = {
    s"$bearingString ${distString(m_to_distUnit)}"
  }

}
