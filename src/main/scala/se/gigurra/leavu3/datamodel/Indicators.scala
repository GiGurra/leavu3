package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}


case class Indicators(source: SourceData = Map.empty) extends SafeParsed[Indicators.type] {
  val nav               = parse(schema.nav)
  //val beacons           = parse(schema.beacons)
  val engines           = parse(schema.engines)
 /* val mechIndicators    = parse(schema.mechIndicators)
  val failureIndicators = parse(schema.failureIndicators)
  val hsiIndicators     = parse(schema.hsiIndicators)*/
}

object Indicators extends Schema[Indicators] {
  val nav               = required[NavIndicators]("nav", default = NavIndicators())
  val beacons           = required[BeaconIndicators]("beacon", default = BeaconIndicators())
  val engines           = required[EngineIndicators]("engines", default = EngineIndicators())
  val mechIndicators    = required[MechIndicators]("mech", default = MechIndicators())
  val failureIndicators = required[FailureIndicators]("failures", default = FailureIndicators())
  val hsiIndicators     = required[HsiIndicators]("HSI", default = HsiIndicators())
}