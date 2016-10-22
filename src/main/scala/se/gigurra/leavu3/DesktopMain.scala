package se.gigurra.leavu3

import java.lang.management.ManagementFactory
import javax.swing.JOptionPane

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import se.gigurra.leavu3.app.{App, Version}
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.interfaces._
import se.gigurra.leavu3.windowstweaks.WindowTweaks
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.{Capture, Logging}

import scala.util.{Failure, Success, Try}

object DesktopMain extends Logging {

  def main(args: Array[String]): Unit = Try {

    Capture.stdOutToFile(s"leavu3-debug-log.txt", append = true)
    Capture.stdErrToFile(s"leavu3-log.txt", append = true)

    logSelectedGarbageCollectors()

    logger.info(s"Starting leavu version: $Version")

    val config = loadConfig(args.headOption.getOrElse("leavu3-cfg.json"))

    DcsRemote.init(config)

    val lwjglConfig = loadLwjglConfig(config)
    val appListener = new App(config, () => onCreate(config), () => onEveryFrame(config))
    val drawable = new DynamicFpsLwjglApplication(appListener, lwjglConfig)
    Gdx.input.setInputProcessor(appListener)

    GameIn.start(config, drawable)
    Dlink.start(config)
    Keyboard.start(config, drawable)

  } match {
    case Success(_) =>
    case Failure(e) =>
      JOptionPane.showMessageDialog(null, Option(e.getMessage).getOrElse("") + Option(e.getCause).fold("")("\n - " + _.getMessage), s"Leavu 3 failed", JOptionPane.ERROR_MESSAGE)
      logger.error(e, s"Leavu 3 failed")
      System.exit(1)
  }

  private def onCreate(config: Configuration): Unit = {
    WindowTweaks.apply(config)
  }

  private def onEveryFrame(config: Configuration): Unit = {
    WindowTweaks.updateStateMachine(config)
  }

  private def loadLwjglConfig(config: Configuration): LwjglApplicationConfiguration = {
    System.setProperty("org.lwjgl.opengl.Window.undecorated", config.borderless.toString)
    new LwjglApplicationConfiguration {
      title = s"${config.title} (${config.instrument.split('.').last})"
      x = config.x
      y = config.y
      width = config.width
      height = config.height
      forceExit = config.forceExit
      vSyncEnabled = config.vSyncEnabled
      foregroundFPS = 1 // We will override this and trigger drawing ourselves
      backgroundFPS = 1 // We will override this and trigger drawing ourselves
      samples = config.aaSamples
      resizable = !config.borderless
    }
  }

  private def logSelectedGarbageCollectors(): Unit = {
    import scala.collection.JavaConversions._
    for (gcMxBean <- ManagementFactory.getGarbageCollectorMXBeans) {
      logger.info(s"Using GC: ${gcMxBean.getName}/${gcMxBean.getObjectName}")
    }
  }

  private def loadConfig(path: String): Configuration = {
    val config = Configuration.readFromFile(path)
    logger.info(s"Config:\n ${JSON.write(config)}")
    config
  }

}
