package se.gigurra.leavu3

import com.github.gigurra.heisenberg.{MapDataProducer, MapDataParser}

/**
  * Created by kjolh on 3/12/2016.
  */
package object datamodel {

  implicit val vec3MapDataParser = new MapDataParser[Vec3] {
    override def parse(field: Any): Vec3 = {
      val data = field.asInstanceOf[Map[String, Number]]
      val dcsX_North = data("x").floatValue
      val dcsY_Up = data("y").floatValue
      val dcsZ_East = data("z").floatValue
      Vec3(dcsZ_East, dcsX_North, dcsY_Up)
    }
  }

  implicit val vec3MapDataProducer = new MapDataProducer[Vec3] {
    override def produce(t: Vec3): Any = {
      val dcsX_North = t.y
      val dcsY_Up = t.z
      val dcsZ_East = t.x
      Map("x" -> dcsX_North, "y" -> dcsY_Up, "z" -> dcsZ_East)
    }
  }
}
