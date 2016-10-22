package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Schema, Parsed}

import scala.language.implicitConversions

case class Target(source: SourceData) extends SafeParsed[Target.type] {
  val contact = parse(schema.contact)
  val dlz     = parse(schema.dlz)
}

object Target extends Schema[Target] {
  val contact = required[Contact]("target")
  val dlz     = required[Dlz]("DLZ")
  implicit def target2Contact(target: Target): Contact = target.contact
}

