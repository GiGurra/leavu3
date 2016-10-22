package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class LeftRight(source: SourceData = Map.empty) extends SafeParsed[LeftRight.type] {
  val left  = parse(schema.left)
  val right = parse(schema.right)
  def total = left + right
}

object LeftRight extends Schema[LeftRight] {
  val left  = required[Float]("left", default = 0)
  val right = required[Float]("right", default = 0)
}

