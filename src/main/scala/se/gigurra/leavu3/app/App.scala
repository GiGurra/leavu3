package se.gigurra.leavu3.app

import com.badlogic.gdx.{ApplicationAdapter, InputAdapter, InputProcessor}
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.interfaces._
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.util.{Failure, Success, Try}
import scala.language.implicitConversions

/**
  * Created by kjolh on 3/20/2016.
  */
class App(appCfg: Configuration,
          onCreate: () => Unit,
          onEveryFrame: () => Unit) extends ApplicationAdapter with Logging {

  val instrument = App.spawnInstrument(appCfg)

  override def render(): Unit = {
    while(!Keyboard.inputQue.isEmpty)
      instrument.keyPressed(Keyboard.inputQue.poll)
    instrument.update(GameIn.snapshot, Dlink.In.ownTeam.toSeq.sortBy(_._1))
    DcsRemote.ownPriority = instrument.priority
    onEveryFrame()
  }

  override def create(): Unit = {
    onCreate()
  }

  val input = new InputAdapter {
    override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
      instrument.mouseClicked(MouseClick(x,y,button))
      true
    }
  }

}

object App extends Logging {

  implicit def toInputAdapter(app: App): InputProcessor = app.input

  def spawnInstrument(appCfg: Configuration): Instrument = {

    val instrumentClassName = appCfg.instrument
    val instrumentClass: Class[Instrument] =
      Try(Class.forName(instrumentClassName)) match {
        case Success(cls) =>
          cls.asInstanceOf[Class[Instrument]]
        case Failure(e) =>
          logger.error(s"Could not find instrument $instrumentClassName - Check your spelling")
          throw e
      }
    logger.info(s"Creating instrument: $instrumentClass")
    instrumentClass.getConstructor(classOf[Configuration]).newInstance(appCfg)
  }
}
