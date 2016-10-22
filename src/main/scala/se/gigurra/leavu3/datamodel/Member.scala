package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema

/**
  * Created by kjolh on 3/20/2016.
  */

case class Member(source: SourceData = Map.empty) extends SafeParsed[Member.type] {
  val planeId         = parse(schema.planeId)
  val modelTime       = parse(schema.modelTime)
  val position        = parse(schema.position)
  val velocity        = parse(schema.velocity)
  val selfData        = parse(schema.selfData)
  val targets         = parse(schema.targets)
  val marks           = parse(schema.markPos)
  val pylons          = parse(schema.pylons)
  val mode            = parse(schema.mode)
  val sensor          = parse(schema.sensor)
  val leavuVersion    = parse(schema.leavuVersion)
  def pitch           = selfData.pitch
  def roll            = selfData.roll
  def heading         = selfData.heading
  def lag             = self.modelTime - modelTime
  def isInSameMission = math.abs(lag) < 5.0
}

object Member extends Schema[Member] {
  val planeId   = required[Int]("playerPlaneId", default = 0)
  val modelTime = required[Double]("modelTime", default = 0)
  val position  = required[Vec3]("position", default = Vec3())
  val velocity  = required[Vec3]("velocity", default = Vec3())
  val selfData  = required[SelfData]("selfData", default = SelfData())
  val targets   = required[Seq[Target]]("targets", default = Seq.empty)
  val markPos   = required[Map[String, Mark]]("markPos", default = Map.empty[String, Mark])
  val pylons    = required[Map[String, DlinkPylon]]("pylons", default = Map.empty[String, DlinkPylon])
  val mode      = required[AircraftMode]("mode", default = AircraftMode())
  val sensor    = required[SensorsStatus]("sensor", default = SensorsStatus())
  val leavuVersion = optional[String]("leavuVersion")
}
