package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Configuration, DlinkData, GameData, Waypoint}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{PpiProjection, Projection, ScreenProjection}
import se.gigurra.leavu3.interfaces.MouseClick
import se.gigurra.leavu3.util.CircleBuffer
import se.gigurra.serviceutils.twitter.logging.Logging

abstract class Page(val name: String)(implicit config: Configuration) extends Logging {

  val stdTextSize = 0.75f
  val ppiProjection = new PpiProjection
  val screenProjection = new ScreenProjection
  val displayUnits = DisplayUnits.displayUnits.setBy(_.name == config.initialUnits)
  val shortName = this.getClass.getSimpleName.toLowerCase.subSequence(0, 3)
  logger.info(s"Created $shortName mfd page")

  def distScale: CircleBuffer[Double] = displayUnits.distScale

  def mouseClicked(click: MouseClick): Unit =  {}

  def pressOsb(i: Int): Unit = {}

  def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit


  //////////////////////////////////////////////////////////////////////////////
  // Common symbols

  protected def drawWpByIndex[_: Projection](wp: Waypoint,
                                             selected: Boolean = false): Unit = {

    val text = if (wp.index > 0) (wp.index - 1).toString else "x"
    drawWp(wp, Some(text), selected)
  }

  protected def drawWp[_: Projection](wp: Waypoint,
                                     text: Option[String],
                                     selected: Boolean = false): Unit ={

    circle(at = wp.position - self.position, radius = 0.015 * symbolScale, typ = if (selected) FILL else LINE, color = WHITE)

    text foreach { text =>
      batched {
        at(wp.position) {
          text.drawRightOf(scale = stdTextSize, color = WHITE)
        }
      }
    }
  }

  protected def drawSelf[_: Projection](surroundCircleRadius: Double): Unit = {
    transform(_.rotate(-self.heading)) {
      circle(surroundCircleRadius, color = DARK_GRAY, typ = LINE)
      lines(shapes.self.coords * symbolScale, CYAN)
      circle(0.005 * symbolScale, color = CYAN, typ = FILL)
    }
  }
}
