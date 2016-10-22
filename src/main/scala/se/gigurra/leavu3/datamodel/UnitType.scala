package se.gigurra.leavu3.datamodel

import com.twitter.util.{Duration, Future, NonFatal}
import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema
import se.gigurra.leavu3.interfaces.DcsRemote
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.collection.concurrent
import scala.language.implicitConversions

case class UnitTypeData(fullName: String,
                        shortName: String,
                        isKnown: Boolean,
                        typ: UnitType.TYPE,
                        power2RangeExponent: Double,
                        maxRangeIndication: Double)

object UnitTypeData {
  val DEFAULT_POWER_MAPPING_EXPONENT = 2.0
  val AWACS_POWER_MAPPING_EXPONENT = 7.0
  val EW_POWER_MAPPING_EXPONENT = 32.0
  val DEFAULT_MAX_RANGE_INDICATION = 1.0
  val DEFAULT_ARH_MAX_RANGE_INDICATION = 0.4
  val UKN = UnitTypeData("unknown", "UKN", isKnown = false, (0,0,0,0), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

  def defaultShortName(fullName: String) =Data(fullName.split(' ').head.toUpperCase, DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

  case class Data(shortName: String, power2RangeExponent: Double, maxRangeIndication: Double)

  def firstNumber(text: String): String = {
    val iFirst = text.indexWhere(_.isDigit)
    if (iFirst >= 0) {
      val iLast = text.drop(iFirst).indexWhere(!_.isDigit)
      if (iLast > 0) {
        text.substring(iFirst, iLast + iFirst)
      } else {
        text.substring(iFirst, iFirst + 1)
      }
    } else {
      "U"
    }
  }

  def mappings(sep: Boolean): PartialFunction[String, Data] = {

    // Aircraft
    case x if x.startsWith("mig-29")                    => Data("29",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("mig-")                      => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-27")                     => Data("29",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-33")                     => Data("29",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-3")                      => Data("30",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("su-")                       => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("f-")                        => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("m-2000")                    => Data("M2",           DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("e-3")                       => Data("E3",           AWACS_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("a-50")                      => Data("50",           AWACS_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    // SAMs
    case x if x.startsWith("s-300ps 40b6md sr") & sep   => Data("CS", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("s-300ps 64h6e sr") & sep    => Data("BB", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("s-300ps 40b6m tr") & sep    => Data("10", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("s-300")                     => Data("10", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("hawk cwar") & sep           => Data("55", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("hawk sr") & sep             => Data("HA", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("hawk tr") & sep             => Data("46", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("hawk")                      => Data("HK", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("buk 9s18m1 sr") & sep       => Data("SD", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("buk 9a310m1 ln") & sep      => Data("11", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("buk")                       => Data("11", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("patriot-rls_p_1") & sep     => Data("FFB", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("patriot") & sep             => Data("P",  DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("patriot")                   => Data("PT", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("mt-lb_p_1")                 => Data("3", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("kub")                       => Data("6", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("csa")                       => Data("8",  DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("osa")                       => Data("8", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("tor")                       => Data("15", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("tunguska")                  => Data("19", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("dog")                       => Data("DE", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("roland")                    => Data("RO", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("shilka zsu-23-4")           => Data("AA", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("sa-")                       => Data(firstNumber(x), DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    // EWRs - 1L13, 55G6
    case x if x.startsWith("1l13")                      => Data("EW", EW_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("55g6")                      => Data("EW", EW_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    // Ships
    case x if x.startsWith("piotr")                     => Data("HN", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("molniya")                   => Data("PS", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("rezky")                     => Data("TP", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("takr")                      => Data("SW", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("moscow")                    => Data("T2", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("neustr")                    => Data("TP", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("albatros")                  => Data("HP", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("sg-47")                     => Data("AE", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("ffg-7")                     => Data("49", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)
    case x if x.startsWith("cvn-70")                    => Data("48", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_MAX_RANGE_INDICATION)

    // Missiles
    case x if x.startsWith("aim-120")                   => Data("M120", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)
    case x if x.startsWith("aim-154")                   => Data("M54", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)
    case x if x.startsWith("r-77")                      => Data("M77", DEFAULT_POWER_MAPPING_EXPONENT, DEFAULT_ARH_MAX_RANGE_INDICATION)

  }

  def apply(fullName: String, isKnown: Boolean, typ: UnitType.TYPE)(implicit cfg: Configuration): UnitTypeData = {
    val data = mappings(cfg.rwrSeparateSrTr).applyOrElse(fullName.toLowerCase, defaultShortName)
    UnitTypeData(fullName, data.shortName, isKnown, typ, data.power2RangeExponent, data.maxRangeIndication)
  }
}

case class UnitType(source: SourceData = Map.empty) extends SafeParsed[UnitType.type] {
  val level1 = parse(schema.level1)
  val level2 = parse(schema.level2)
  val level3 = parse(schema.level3)
  val level4 = parse(schema.level4)

  def typ: UnitType.TYPE = {
    (level1, level2, level3, level4)
  }

  def isFlyer: Boolean = level1 == UnitType.AIR || level1 == UnitType.WEAPON
}

object UnitType extends Schema[UnitType] with Logging {
  val level1 = required[Int]("level1", default = 0)
  val level2 = required[Int]("level2", default = 0)
  val level3 = required[Int]("level3", default = 0)
  val level4 = required[Int]("level4", default = 0)

  val AIR = 1
  val GROUND = 2
  val NAVY = 3
  val WEAPON = 4
  val STATIC = 5
  val DESTROYED = 6

  type TYPE = (Int, Int, Int, Int)
  val mappedTypes = new concurrent.TrieMap[TYPE, UnitTypeData]()

  def getData(t: UnitType)(implicit configuration: Configuration): UnitTypeData = {

    if (!mappedTypes.contains(t.typ)) {

      val script =s"{name=LoGetNameByType${t.typ}}"
      val url = s"export/$script"

      DcsRemote.get(url, maxAge = Some(Duration.fromSeconds(3600))).map { json =>
        val name = JSON.readMap(json)("name").asInstanceOf[String]
        mappedTypes.put(t.typ, UnitTypeData.apply(name, isKnown = true, t.typ))
        logger.info(s"Mapped type ${t.typ} -> $name")
      }

    }

    mappedTypes.getOrElse(t.typ, UnitTypeData.UKN)
  }

  implicit def UnitType2Data(t: UnitType)(implicit configuration: Configuration): UnitTypeData = getData(t)
}
