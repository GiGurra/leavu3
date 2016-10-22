package se.gigurra.leavu3

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import com.twitter.util.{Duration, JavaTimer}
import se.gigurra.leavu3.gfx.Drawable
import se.gigurra.leavu3.util.DefaultTimer
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.util.control.NonFatal

/**
  * Created by kjolh on 3/20/2016.
  */
class DynamicFpsLwjglApplication(listener: ApplicationListener, config: LwjglApplicationConfiguration)
  extends LwjglApplication(listener, config)
    with Drawable
    with Logging {

  @volatile private var hasDrawn = false

  // Minimum frame rate drawing (10 fps)
  DefaultTimer.fps(10) {
    if (!hasDrawn)
      draw()
    hasDrawn = false
  }

  def draw(): Unit = {
    try {
      mainLoopThread.interrupt()
      hasDrawn = true
    } catch {
      case NonFatal(e) =>
        logger.error(e, s"Failed to trigger redraw")
    }
  }

}
