package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

case class Rwr(source: SourceData = Map.empty) extends SafeParsed[Rwr.type] {
  val emitters = parse(schema.emitters)
  val mode     = parse(schema.mode)
}

object Rwr extends Schema[Rwr] {
  val emitters = required[Seq[Emitter]]("Emitters", default = Seq.empty)
  val mode     = required[Int]("Mode", default = 0)
}
