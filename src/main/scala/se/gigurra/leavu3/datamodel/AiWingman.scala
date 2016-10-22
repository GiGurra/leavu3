package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class AiWingman(source: SourceData) extends SafeParsed[AiWingman.type] {
  val orderedTask     = parse(schema.orderedTask)
  val currentTarget   = parse(schema.currentTarget)
  val currentTask     = parse(schema.currentTask)
  val spatial         = parse(schema.wingmenPosition)
  val id              = parse(schema.wingmenId)
  val orderedTarget   = parse(schema.orderedTarget)

  def position = spatial.position
  def pitch = spatial.pitch
  def roll = spatial.roll
  def heading = spatial.heading
}

object AiWingman extends Schema[AiWingman] {
  val orderedTask     = required[String]("ordered_task")
  val currentTarget   = required[Int]("current_target")
  val currentTask     = required[String]("current_task")
  val wingmenPosition = required[Spatial]("wingmen_position")
  val wingmenId       = required[Int]("wingmen_id")
  val orderedTarget   = required[Int]("ordered_target")
}

