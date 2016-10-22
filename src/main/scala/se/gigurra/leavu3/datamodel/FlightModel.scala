package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class FlightModel(source: SourceData = Map.empty) extends SafeParsed[FlightModel.type] {
  /*val pitch             = parse(schema.pitch).toDegrees
  val roll              = parse(schema.roll).toDegrees
  val trueHeading       = parse(schema.trueHeading).toDegrees
  val magneticHeading   = parse(schema.magneticHeading).toDegrees
  val angleOfAttack     = parse(schema.angleOfAttack).toDegrees
*/
  val velocity          = parse(schema.velocity)
 /* val acceleration      = parse(schema.acceleration)

  val indicatedAirspeed = parse(schema.indicatedAirspeed)
  val trueAirspeed      = parse(schema.trueAirspeed)
  val verticalVelocity  = parse(schema.verticalVelocity)
  val machNumber        = parse(schema.machNumber)

  val altitudeAGL       = parse(schema.altitudeAGL)
  val altitudeAsl       = parse(schema.altitudeAsl)

  val windVelocity      = parse(schema.windVelocity)
  val airPressure       = parse(schema.airPressure)
  val slipBallDeviation = parse(schema.slipBallDeviation)

  val ilsLocalizer      = parse(schema.ilsLocalizer)
  val ilsGlideslope     = parse(schema.ilsGlideslope)*/
}

object FlightModel extends Schema[FlightModel] {
  val pitch             = required[Float]("pitch", default = 0)
  val roll              = required[Float]("roll", default = 0)
  val trueHeading       = required[Float]("heading", default = 0)
  val magneticHeading   = required[Float]("magneticYaw", default = 0)
  val angleOfAttack     = required[Float]("AOA", default = 0)

  val velocity          = required[Vec3]("vectorVelocity", default = Vec3())
  val acceleration      = required[Vec3]("acc", default = Vec3())

  val indicatedAirspeed = required[Float]("IAS", default = 0)
  val trueAirspeed      = required[Float]("TAS", default = 0)
  val verticalVelocity  = required[Float]("vv", default = 0)
  val machNumber        = required[Float]("mach", default = 0)

  val altitudeAGL       = required[Float]("altitudeAboveGroundLevel", default = 0)
  val altitudeAsl       = required[Float]("altitudeAboveSeaLevel", default = 0)

  val windVelocity      = required[Vec3]("windVectorVelocity", default = Vec3())
  val airPressure       = required[Float]("atmospherePressure", default = 0)
  val slipBallDeviation = required[Float]("slipBallPosition", default = 0)

  val ilsLocalizer      = required[Float]("sideDeviation", default = 0)
  val ilsGlideslope     = required[Float]("glideDeviation", default = 0)
}

