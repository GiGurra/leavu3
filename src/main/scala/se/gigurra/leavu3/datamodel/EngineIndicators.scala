package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class EngineIndicators(source: SourceData = Map.empty) extends SafeParsed[EngineIndicators.type] {
  val engineStart       = parse(schema.engineStart)
  val hydraulicPressure = parse(schema.hydraulicPressure)
  val fuelConsumption   = parse(schema.fuelConsumption)
  val rpm               = parse(schema.rpm)
  val temperature       = parse(schema.temperature)
  val fuelInternal      = parse(schema.fuelInternal)
  val fuelExternal      = parse(schema.fuelExternal)

  def fuelTotal = fuelInternal + fuelExternal
}

object EngineIndicators extends Schema[EngineIndicators] {
  val engineStart       = required[LeftRight]("EngineStart", default = LeftRight())
  val hydraulicPressure = required[LeftRight]("HydraulicPressure", default = LeftRight())
  val fuelConsumption   = required[LeftRight]("FuelConsumption", default = LeftRight())
  val rpm               = required[LeftRight]("RPM", default = LeftRight())
  val temperature       = required[LeftRight]("Temperature", default = LeftRight())
  val fuelInternal      = required[Double]("fuel_internal", default = 0)
  val fuelExternal      = required[Double]("fuel_external", default = 0)
}
