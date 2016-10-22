package se.gigurra.leavu3.datamodel


import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class HsiIndicators(source: SourceData = Map.empty) extends SafeParsed[HsiIndicators.type] {
  val rmiRaw          = parse(schema.rmiRaw).toDegrees
  val courseDeviation = parse(schema.courseDeviation).toDegrees
  val course          = parse(schema.course).toDegrees
  val headingRaw      = parse(schema.headingRaw).toDegrees
  val headingPointer  = parse(schema.headingPointer).toDegrees
  val bearingPointer  = parse(schema.bearingPointer).toDegrees
  val adfRaw          = parse(schema.adfRaw).toDegrees
}

object HsiIndicators extends Schema[HsiIndicators] {
  val rmiRaw          = required[Float]("RMI_raw", default = 0)
  val courseDeviation = required[Float]("CourseDeviation", default = 0)
  val course          = required[Float]("Course", default = 0)
  val headingRaw      = required[Float]("Heading_raw", default = 0)
  val headingPointer  = required[Float]("HeadingPointer", default = 0)
  val bearingPointer  = required[Float]("BearingPointer", default = 0)
  val adfRaw          = required[Float]("ADF_raw", default = 0)
}


