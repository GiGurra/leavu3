package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema

case class Leavu3Instance(source: SourceData = Map.empty) extends SafeParsed[Leavu3Instance.type] {
  val id              = parse(schema.id)
  val priority        = parse(schema.priority)
  val isActingMaster  = parse(schema.isActingMaster)
}

object Leavu3Instance extends Schema[Leavu3Instance] {
  val id              = required[String]  ("id",              default = "")
  val priority        = required[Int]     ("priority",        default = -1)
  val isActingMaster  = required[Boolean] ("isActingMaster",  default = true)

  def apply(id: String, priority: Int, isActingMaster: Boolean): Leavu3Instance = marshal(
    this.id -> id,
    this.priority -> priority,
    this.isActingMaster -> isActingMaster
  )
}
