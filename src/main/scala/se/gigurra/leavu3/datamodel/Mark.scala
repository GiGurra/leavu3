package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema
import se.gigurra.leavu3.util.CurTime

/**
  * Created by kjolh on 3/20/2016.
  */
case class Mark(source: SourceData = Map.empty) extends SafeParsed[Mark.type] {
  val id        = parse(schema.id)
  val position  = parse(schema.position)
  val timestamp = parse(schema.timestamp)
}

object Mark extends Schema[Mark] {
  val id        = required[String]("id", default = "")
  val position  = required[Vec3]("position", default = Vec3())
  val timestamp = required[Double]("timestamp", default = CurTime.seconds)

  def apply(id: String, position: Vec3): Mark = marshal(
    this.id -> id,
    this.position -> position
  )
}

