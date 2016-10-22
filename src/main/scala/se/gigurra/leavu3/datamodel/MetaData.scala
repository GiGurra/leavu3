package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class MetaData(source: SourceData = Map.empty) extends SafeParsed[MetaData.type] {
  //val camera    = parse(schema.camera)
  val planeId   = parse(schema.planeId)
  val modelTime = parse(schema.modelTime)
  //val version   = parse(schema.version)
  val selfData  = parse(schema.selfData)
}

object MetaData extends Schema[MetaData] {
  val camera    = required[Spatial]("camPos", default = Spatial())
  val planeId   = required[Int]("playerPlaneId", default = 0)
  val modelTime = required[Double]("modelTime", default = 0)
  val version   = required[GameVersion]("version", default = GameVersion())
  val selfData  = required[SelfData]("selfData", default = SelfData())
}