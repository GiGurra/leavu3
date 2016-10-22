package se.gigurra.leavu3.datamodel

import com.twitter.util.NonFatal
import com.github.gigurra.heisenberg._

/**
  * Because dcs sometimes sends nulls... :P
  */
abstract class SafeParsed[S <: Schema[_]](implicit schema: S) extends Parsed[S]() {

  override protected def parse[FieldType](field: FieldOption[FieldType], orElse: => Option[FieldType]): Option[FieldType] = {
    try {
      super.parse(field, orElse)
    } catch {
      case NonFatal(e) => field.getDefault
    }
  }

  override protected def parse[FieldType](field: FieldOption[FieldType]): Option[FieldType] = {
    try {
      super.parse(field)
    } catch {
      case NonFatal(e) => field.getDefault
    }
  }

  override protected def parse[FieldType](field: FieldRequired[FieldType], orElse: => FieldType): FieldType = {
    try {
      super.parse(field, orElse)
    } catch {
      case NonFatal(e) =>
        field.getDefault.getOrElse(throw new RuntimeException(s"No default value set for field $field on ${this.getClass}"))
    }
  }

  override protected def parse[FieldType](field: FieldRequired[FieldType]): FieldType = {
    try {
      super.parse(field)
    } catch {
      case NonFatal(e) =>
        field.getDefault.getOrElse(throw new RuntimeException(s"No default value set for field $field on ${this.getClass}"))
    }
  }

}
