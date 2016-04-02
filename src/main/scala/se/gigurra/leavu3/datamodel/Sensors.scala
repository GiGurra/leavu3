package se.gigurra.leavu3.datamodel

import se.gigurra.heisenberg.MapData._
import se.gigurra.heisenberg.{Schema, Parsed}

case class SensorsWire(source: SourceData = Map.empty) extends SafeParsed[SensorsWire.type] {
  val status = parse(schema.status)
  val targets = parse(schema.targets)

  def toSensors: Sensors = Sensors(status, targets.toTargets)
}

case class Sensors(status: SensorsStatus = SensorsStatus(),
                   targets: Targets = Targets()) {
  def pdt: Option[Target] = targets.pdt
  def withRwsMemory(rwsContacts: Seq[Contact]): Sensors = copy(targets = targets.withRwsMemory(rwsContacts))
}

object SensorsWire extends Schema[SensorsWire] {
  val status  = required[SensorsStatus]("status", default = SensorsStatus())
  val targets = required[TargetsWire]("targets", default = TargetsWire())
}

