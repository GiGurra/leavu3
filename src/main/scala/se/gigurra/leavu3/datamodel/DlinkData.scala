package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema
import se.gigurra.leavu3.util.CurTime
import scala.language.implicitConversions

/**
  * Created by kjolh on 3/20/2016.
  */

case class DlinkData(source: SourceData = Map.empty) extends SafeParsed[DlinkData.type] {
  val timestamp = parse(schema.timestamp)
  val data      = parse(schema.data)
}

object DlinkData extends Schema[DlinkData] {
  val timestamp = required[Double]("timestamp", default = CurTime.seconds)
  val data = required[Member]("data", default = Member())

  implicit def toMember(d: DlinkData): Member = d.data
}

case class RawDlinkData(source: SourceData = Map.empty) extends SafeParsed[RawDlinkData.type] {
}

object RawDlinkData extends Schema[RawDlinkData] {
}