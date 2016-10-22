package se.gigurra.leavu3.datamodel

import java.io.FileNotFoundException
import javax.swing.JOptionPane

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.{Parsed, Schema}
import se.gigurra.leavu3.mfd.DefaultMfdKeyBindings
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}

case class Configuration(source: SourceData = Map.empty) extends Parsed[Configuration.type] {

  val title             = parse(schema.title)
  val x                 = parse(schema.x)
  val y                 = parse(schema.y)
  val width             = parse(schema.width)
  val height            = parse(schema.height)
  val forceExit         = parse(schema.forceExit)
  val vSyncEnabled      = parse(schema.vSyncEnabled)
  val gameDataFps       = parse(schema.gameDataFps)
  val dcsRemoteAddress  = parse(schema.dcsRemoteAddress)
  val dcsRemotePort     = parse(schema.dcsRemotePort)
  val aaSamples         = parse(schema.aaSamples)
  val instrument        = parse(schema.instrument)
  val symbolScale       = parse(schema.symbolScale)
  val borderless        = parse(schema.borderless)
  val noFocusOnClick    = parse(schema.noFocusOnClick)
  val alwaysOnTop       = parse(schema.alwaysOnTop)
  val rwrSeparateSrTr   = parse(schema.rwrSeparateSrTr)
  val initialUnits      = parse(schema.units)
  val initialQp         = parse(schema.initialQp)
  val qps               = parse(schema.qps)
  val use3dBscope       = parse(schema.use3dBscope)
  val hsdHsi            = parse(schema.hsdHsi)
  val hsdModes          = parse(schema.hsdModes)
  val hsdHeading        = parse(schema.hsdHeading)
  val osbs              = parse(schema.osbs)
  val dclt              = parse(schema.dclt)
  val keyInputEnabled   = parse(schema.keyInputEnabled)
  val keyBindings       = parse(schema.keyBindings)
  val forcedSlave       = parse(schema.forcedSlave)

  if (parse(schema.keyBindingOffset) != 0)
    JOptionPane.showMessageDialog(null, "The 'keyBindingOffset' option no longer exists \n ->Specify actual key bindings instead in the config file!")

  if (!parse(schema.relayDlink))
    JOptionPane.showMessageDialog(null, "The 'relayDlink' option no longer exists (it has been automated)\n Please remove it from your config file")

  if (parse(schema.slaveMode))
    JOptionPane.showMessageDialog(null, "The 'slaveMode' option no longer exists (it has been automated) \n Please remove it from your config file")

  require(title.length > 0, "Configured title must be > 0")
  require(width > 0, "Configured width must be > 0")
  require(height > 0, "Configured height must be > 0")
  require(initialUnits == "imperial" || initialUnits == "metric", "Unknown units specified i confiuration")
  require(initialQp >= 0, "config:initialQp must be >= 0")
  require(initialQp < 5, "config:initialQp must be < 5")
}

object Configuration extends Schema[Configuration] with Logging {
  val title             = required[String]      ("title",             default = "Leavu 3")
  val x                 = required[Int]         ("x",                 default = 200)
  val y                 = required[Int]         ("y",                 default = 200)
  val width             = required[Int]         ("width",             default = 1024)
  val height            = required[Int]         ("height",            default = 1024)
  val forceExit         = required[Boolean]     ("forceExit",         default = false)
  val vSyncEnabled      = required[Boolean]     ("vSyncEnabled",      default = true)
  val gameDataFps       = required[Int]         ("gameDataFps",       default = 40)
  val dcsRemoteAddress  = required[String]      ("dcsRemoteAddress",  default = "127.0.0.1")
  val dcsRemotePort     = required[Int]         ("dcsRemotePort",     default = 12340)
  val aaSamples         = required[Int]         ("aaSamples",         default = 4)
  val instrument        = required[String]      ("instrument",        default = "se.gigurra.leavu3.mfd.Mfd")
  val symbolScale       = required[Float]       ("symbolScale",       default = 1.0f)
  val relayDlink        = required[Boolean]     ("relayDlink",        default = true)
  val borderless        = required[Boolean]     ("borderless",        default = false)
  val noFocusOnClick    = required[Boolean]     ("noFocusOnClick",    default = false)
  val alwaysOnTop       = required[Boolean]     ("alwaysOnTop",       default = false)
  val keyBindingOffset  = required[Int]         ("keyBindingOffset",  default = 0)
  val rwrSeparateSrTr   = required[Boolean]     ("rwrSeparateSrTr",   default = false)
  val units             = required[String]      ("units",             default = "imperial")
  val initialQp         = required[Int]         ("initialQp",         default = 0)
  val qps               = required[Seq[String]] ("qps",               default = Seq("HSD", "RWR", "SMS", "FCR", "INF"))
  val use3dBscope       = required[Boolean]     ("use3dBscope",       default = true)
  val hsdHsi            = required[Boolean]     ("hsd-hsi",           default = true)
  val hsdModes          = required[Boolean]     ("hsd-modes",         default = true)
  val hsdHeading        = required[Boolean]     ("hsd-heading",       default = true)
  val osbs              = required[Boolean]     ("osbs",              default = true)
  val dclt              = required[Boolean]     ("dclt",              default = false)
  val slaveMode         = required[Boolean]     ("slave-mode",        default = false)
  val keyInputEnabled   = required[Boolean]     ("keyInputEnabled",   default = true)
  val forcedSlave       = required[Boolean]     ("forcedSlave",       default = false)
  val keyBindings       = required[Map[String, String]] ("keyBindings", default = DefaultMfdKeyBindings())

  def readFromFile(s: String = "leavu3-cfg.json"): Configuration = {
    logger.info(s"Loading configuration file: $s")
    Try(JSON.read[Configuration](scala.io.Source.fromFile(s).mkString)) match {
      case Success(cfg) => cfg
      case Failure(e: FileNotFoundException) =>
        logger.info(s"No config file found (path=$s) - using default configuration")
        Configuration()
      case Failure(e) =>
        logger.fatal(e, s"Failed to read config file ($e) '$s'")
        throw e
    }
  }
}

case class DlinkConfiguration(source: SourceData = Map.empty) extends Parsed[DlinkConfiguration.type] {
  val host         = parse(schema.host)
  val port         = parse(schema.port)
  val team         = parse(schema.team)
  val callsign     = parse(schema.callsign)
}

object DlinkConfiguration extends Schema[DlinkConfiguration] {
  val host         = required[String]  ("host",         default = "build.culvertsoft.se")
  val port         = required[Int]     ("port",         default = 12340)
  val team         = required[String]  ("team",         default = "BLUE_RABBITS")
  val callsign     = required[String]  ("callsign",     default = "JarJar")
}

case class MissionConfiguration(source: SourceData = Map.empty) extends Parsed[MissionConfiguration.type] {
  val bingo        = parse(schema.bingo)
  val joker        = parse(schema.joker)
}

object MissionConfiguration extends Schema[MissionConfiguration] {
  val bingo        = required[Double]  ("bingo",        default = 2500.0)
  val joker        = required[Double]  ("joker",        default = 5000.0)
}