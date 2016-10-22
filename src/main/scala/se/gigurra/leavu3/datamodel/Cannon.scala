package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Cannon(source: SourceData = Map.empty) extends SafeParsed[Cannon.type] {
  val shells = parse(schema.shells)
}

object Cannon extends Schema[Cannon] {
  val shells = required[Int]("shells", default = 0)
}

