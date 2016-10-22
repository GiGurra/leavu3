package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema

case class TargetsWire(source: SourceData = Map.empty) extends SafeParsed[TargetsWire.type] {
  val detected = parse(schema.detected)
  val tws      = parse(schema.tws)
  val locked   = parse(schema.locked)

  def toTargets: Targets = Targets(detected, tws, locked)
}

case class Targets(detected: Seq[Contact] = Nil,
                   tws: Seq[Target] = Nil,
                   locked: Seq[Target] = Nil) {

  def all: Seq[Contact] = detected ++ tws.map(_.contact) ++ locked.map(_.contact)
  def pdt: Option[Target] = locked.headOption
  def withRwsMemory(rwsContacts: Seq[Contact]): Targets = copy(detected = rwsContacts)

  def withoutHiddenContacts: Targets = {
    val fromOwnRadar = Contact.FromOwnRadar(order = Map.empty)
    import fromOwnRadar._
    Targets.this.copy(
      detected = detected.filterNot(_.hidden),
      tws = tws.filterNot(_.contact.hidden),
      locked = locked.filterNot(_.contact.hidden)
    )
  }
}

object TargetsWire extends Schema[TargetsWire] {
  val detected = required[Seq[Contact]]("detected", default = Seq.empty)
  val tws      = required[Seq[Target]]("tws", default = Seq.empty)
  val locked   = required[Seq[Target]]("locked", default = Seq.empty)
}

