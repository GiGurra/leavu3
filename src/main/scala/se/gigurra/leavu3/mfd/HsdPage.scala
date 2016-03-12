package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.Configuration
import se.gigurra.leavu3.externaldata.{DlinkInData, DlinkOutData, GameData, Vec2}
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CircleBuffer
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(config: Configuration) extends Page {

  val distance = CircleBuffer(10 nmi, 20 nmi, 40 nmi, 80 nmi, 160 nmi).withDefaultValue(40 nmi)
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  def symbolScale = config.symbolScale * screen2World

  def update(game: GameData, dlinkIn: DlinkInData, dlinkOut: DlinkOutData): Unit = {
    ppi_viewport(viewportSize = distance * 2.0, offs = Vec2(0.0, -distance * deprFactor), heading = self.heading) {
      drawSelf(game)
      drawHsi(game)
      drawWaypoints(game)
      drawAiWingmen(game)
      drawDlinkWingmen(dlinkIn)
      drawDlinkWingmenTargets(dlinkIn)
      drawLockedTargets(game)
    }
    drawMenuItems(game)
  }

  def drawHsi(game: GameData): Unit = {
    circle(radius = distance     * 0.50, color = DARK_GRAY)
    circle(radius = distance     * 1.00)
    circle(radius = distance     * 1.50)
    lines(shapes.hsi.flag     * symbolScale + Vec2(0.0, distance * 0.50))
    lines(shapes.hsi.flag     * symbolScale + Vec2(0.0, distance * 1.00))
    lines(shapes.hsi.flag     * symbolScale + Vec2(0.0, distance * 1.50))
    lines(shapes.hsi.eastPin  * symbolScale + Vec2(distance * 0.50, 0.0))
    lines(shapes.hsi.eastPin  * symbolScale + Vec2(distance * 1.00, 0.0))
    lines(shapes.hsi.eastPin  * symbolScale + Vec2(distance * 1.50, 0.0))
    lines(shapes.hsi.westPin  * symbolScale + Vec2(-distance * 0.50, 0.0))
    lines(shapes.hsi.westPin  * symbolScale + Vec2(-distance * 1.00, 0.0))
    lines(shapes.hsi.westPin  * symbolScale + Vec2(-distance * 1.50, 0.0))
    lines(shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 0.50))
    lines(shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.00))
    lines(shapes.hsi.southPin * symbolScale + Vec2(0.0, -distance * 1.50))
  }

  def drawSelf(game: GameData): Unit = {
    transform(_.rotate(-self.heading)) {
      lines(shapes.self.coords * symbolScale, color = LIGHT_GRAY)
    }
  }

  def drawWaypoints(game: GameData): Unit = {
  }

  def drawAiWingmen(game: GameData): Unit = {
    game.aiWingmen foreach { wingman =>
      circle(at = wingman.position - self.position, radius = 0.025 * symbolScale, color = CYAN)
    }
  }

  def drawDlinkWingmen(dlinkIn: DlinkInData): Unit = {
  }

  def drawDlinkWingmenTargets(dlinkIn: DlinkInData): Unit = {
  }

  def drawLockedTargets(game: GameData): Unit = {
  }

  def drawMenuItems(game: GameData): Unit = {

    batched {
      val text =
        s"""
           |Heading: ${self.heading}
           |Velocity: ${self.velocity}
           |nwps: ${game.route.waypoints.size}""".stripMargin

      transform(_.scalexy(1.5f / text.width)) {
        text.draw()
      }
    }
  }
}
