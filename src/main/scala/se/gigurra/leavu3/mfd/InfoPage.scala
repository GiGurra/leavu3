package se.gigurra.leavu3.mfd

import java.awt.Desktop
import java.net.URI

import com.badlogic.gdx.graphics.Color
import com.twitter.util.{Duration, JavaTimer, Time}
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.gfx.{Blink, ScreenProjection}
import se.gigurra.leavu3.interfaces.{DcsRemote, Dlink, GameIn, MouseClick}
import se.gigurra.leavu3.lmath.Box

import scala.language.postfixOps
import scala.util.Try

/**
  * Created by kjolh on 3/12/2016.
  */
case class InfoPage(implicit dcsRemote: DcsRemote, config: Configuration) extends Page("INF") {

  implicit val projection = new ScreenProjection()
  val OSB_UPDATE_COVER = 2
  val OSB_UPDATE = 3
  val OSB_UPDATE_COVER2 = 4
  val blink = Blink(Seq(true, false), 1.0)
  val clickToUpdateText = "CLICK TO UPDATE"
  val versionUrl = "http://build.culvertsoft.se/dcs/leavu3-version.txt"
  val downloadUrl = "http://build.culvertsoft.se/dcs/"
  val version = Try(scala.io.Source.fromFile("version.txt", "UTF-8").mkString).getOrElse("unknown (version.txt missing)")
  @volatile var latestVersion = "checking..."

  def updateVersion(): Unit = {
    if(Desktop.isDesktopSupported) {
      Desktop.getDesktop.browse(new URI(downloadUrl))
    } else {
      logger.error(s"Can't auto update - browser not supported!")
    }
  }

  val versionGetterTimer = new JavaTimer(isDaemon = true)
  versionGetterTimer.schedule(Time.now, Duration.fromSeconds(2)) {
    latestVersion = Try(scala.io.Source.fromURL(versionUrl, "UTF-8").mkString).getOrElse("unknown")
  }

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_UPDATE_COVER => updateVersion()
      case OSB_UPDATE_COVER2 => updateVersion()
      case OSB_UPDATE => updateVersion()
      case _ =>
    }
  }

  override def mouseClicked(click: MouseClick): Unit =  {
    val center = Mfd.Osb.positions(OSB_UPDATE)
    val height = Mfd.Osb.boxHeight * config.symbolScale
    val width = Mfd.Osb.boxWidth * config.symbolScale * (clickToUpdateText.length.toFloat / 3.0f)
    val hitBox = Box(width, height, center)
    if (hitBox.contains(click.ortho11Raw)) {
      updateVersion()
    }
  }

  override def draw(game: GameData, dlinkIn: Map[String, DlinkData]): Unit = {

    val updateAvailable = version != latestVersion
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { atScreen(-0.8, 0.8) {

      transform(_
        .scalexy(scale)) {

        var n = 0
        val titleLen = 16
        def drawTextLine(title: String, value: Any, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight)){
            val fullString = title.pad(titleLen) + " : " + value
            fullString.drawRaw(xAlign = 0.5f, color = color)
          }
          n += 1
        }

        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("-----VERSION----", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine(" Latest version", latestVersion, LIGHT_GRAY)
        drawTextLine("   Your version", version, if (updateAvailable) YELLOW else LIGHT_GRAY)
        drawTextLine("Update available", if (updateAvailable) "Yes" else "No", if (updateAvailable) YELLOW else LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("-----STATUS-----", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("     DCS Remote", if (GameIn.dcsRemoteConnected) "connected" else "disconnected", if (GameIn.dcsRemoteConnected) GREEN else RED)
        drawTextLine("       DCS Game", if (GameIn.dcsGameConnected) "connected" else "disconnected", if (GameIn.dcsGameConnected) GREEN else RED)
        drawTextLine("          DLink", if (Dlink.connected) "connected" else "disconnected", if (Dlink.connected) GREEN else RED)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----SETTINGS----", "--------------------------------", LIGHT_GRAY)
        drawTextLine("----------------", "--------------------------------", LIGHT_GRAY)
        drawTextLine("      DCS Remote", s"${config.dcsRemoteAddress}:${config.dcsRemotePort}", LIGHT_GRAY)
        drawTextLine("    DLink server", s"${Dlink.config.host}:${Dlink.config.port}", LIGHT_GRAY)
        drawTextLine("      DLink team", Dlink.config.team, LIGHT_GRAY)
        drawTextLine("  DLink callsign", Dlink.config.callsign, LIGHT_GRAY)
        drawTextLine("      DLink mode", if (config.relayDlink) "receive + transmit" else "receive", LIGHT_GRAY)
        drawTextLine("   foregroundFPS", config.foregroundFPS, LIGHT_GRAY)
        drawTextLine("   backgroundFPS", config.backgroundFPS, LIGHT_GRAY)
        drawTextLine("     gameDataFps", config.gameDataFps, LIGHT_GRAY)
        drawTextLine("     symbolScale", config.symbolScale, LIGHT_GRAY)

      }
    }}

    if (updateAvailable) {
      Mfd.Osb.drawHighlighted(OSB_UPDATE, clickToUpdateText, highlighted = blink)
    }
  }

}