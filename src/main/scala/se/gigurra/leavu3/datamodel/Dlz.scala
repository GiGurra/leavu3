package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Dlz(source: SourceData = Map.empty) extends SafeParsed[Dlz.type] {
  val rAero = parse(schema.rAero)
  val rMin  = parse(schema.rMin)
  val rPi   = parse(schema.rPi)
  val rTr   = parse(schema.rTr)
}

object Dlz extends Schema[Dlz] {
  val rAero = required[Float]("RAERO", default = 0)
  val rMin  = required[Float]("RMIN", default = 0)
  val rPi   = required[Float]("RPI", default = 0)
  val rTr   = required[Float]("RTR", default = 0)
}
