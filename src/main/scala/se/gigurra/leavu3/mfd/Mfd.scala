package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.externaldata.{Vec2, Key, KeyPress, GameData}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.{DlinkSettings, Configuration, DlinkData, Instrument}

import scala.language.postfixOps

case class Mfd(implicit config: Configuration, dlinkSettings: DlinkSettings) extends Instrument(config, dlinkSettings) {

  val hsd = HsdPage()
  val rwr = RwrPage()
  val sms = SmsPage()
  val fcr = FcrPage()
  val available = Seq(hsd, rwr, sms, fcr)
  var qPages = Map[Int, Page](0 -> hsd, 1 -> rwr, 2-> sms)
  var iQPage = 0
  var mainMenuOpen: Boolean = false

  def currentPage: Option[Page] = qPages.get(iQPage)

  def qp2Osb(qp: Int): Int = 13 - qp

  def osb2Qp(osb: Int): Int = 13 - osb

  def drawPage(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {
    currentPage.foreach(_.draw(game, dlinkIn))
  }

  def drawMainMenu(): Unit = {
    // TODO: Draw something
  }

  def update(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = frame {
    if (mainMenuOpen) {
      drawMainMenu()
    } else {
      drawPage(game, dlinkIn)
    }
  }

  def keyPressed(press: KeyPress): Unit = {
    if (mainMenuOpen) {
      press match {
        case Key.QP_OSB(i) => pressOsbInMenu(i)
        case Key.OSB(i) => pressOsbInMenu(i)
        case _ =>
      }
    } else {
      press match {
        case Key.QP_OSB(i) => pressQpOsb(osb2Qp(i))
        case Key.OSB(i) => currentPage.foreach(_.pressOsb(i))
        case _ =>
      }
    }
  }

  def pressOsbInMenu(i: Int): Unit = {
    mainMenuOpen = false
  }

  def pressQpOsb(i: Int): Unit = {
    if (i == iQPage)
      mainMenuOpen = !mainMenuOpen
  }
}

object Mfd {
  object Osb {
    val nPerSide = 5
    val boxWidth = 0.10f
    val boxHeight = 0.05f
    val offs = 0.2f
    val inset = 0.1f
    val wholeWidth = 2.0f - offs * 2.0f
    val step = wholeWidth / (nPerSide - 1).toFloat

    val upperLeftBox  = Vec2(-1.0f + offs,   1.0f - inset)
    val upperRightBox = Vec2( 1.0f - inset,  1.0f - offs)
    val lowerRightBox = Vec2( 1.0f - offs,  -1.0f + inset)
    val lowerLeftBox  = Vec2(-1.0f + inset, -1.0f + offs)

    val upperBoxCenters = Seq(
      upperLeftBox + 0.0 * Vec2(step, 0.0),
      upperLeftBox + 1.0 * Vec2(step, 0.0),
      upperLeftBox + 2.0 * Vec2(step, 0.0),
      upperLeftBox + 3.0 * Vec2(step, 0.0),
      upperLeftBox + 4.0 * Vec2(step, 0.0)
    )

    val rightBoxCenters = Seq(
      upperRightBox + 0.0 * Vec2(0.0, -step),
      upperRightBox + 1.0 * Vec2(0.0, -step),
      upperRightBox + 2.0 * Vec2(0.0, -step),
      upperRightBox + 3.0 * Vec2(0.0, -step),
      upperRightBox + 4.0 * Vec2(0.0, -step)
    )

    val lowerBoxCenters = Seq(
      lowerRightBox + 0.0 * Vec2(-step, 0.0),
      lowerRightBox + 1.0 * Vec2(-step, 0.0),
      lowerRightBox + 2.0 * Vec2(-step, 0.0),
      lowerRightBox + 3.0 * Vec2(-step, 0.0),
      lowerRightBox + 4.0 * Vec2(-step, 0.0)
    )

    val leftBoxCenters = Seq(
      lowerLeftBox + 0.0 * Vec2(0.0, step),
      lowerLeftBox + 1.0 * Vec2(0.0, step),
      lowerLeftBox + 2.0 * Vec2(0.0, step),
      lowerLeftBox + 3.0 * Vec2(0.0, step),
      lowerLeftBox + 4.0 * Vec2(0.0, step)
    )

    val positions = Seq(upperBoxCenters, rightBoxCenters, lowerBoxCenters, leftBoxCenters).flatten

  }
}