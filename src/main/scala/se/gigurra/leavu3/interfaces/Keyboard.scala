package se.gigurra.leavu3.interfaces

import java.util.concurrent.ConcurrentLinkedQueue

import com.badlogic.gdx.Gdx
import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import se.gigurra.leavu3.datamodel.{Configuration, Vec2}
import se.gigurra.leavu3.gfx.Drawable
import se.gigurra.leavu3.util.{DefaultTimer, Throttled}
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging

/**
  * Created by kjolh on 3/18/2016.
  */
object Keyboard extends Logging {

  val inputQue = new ConcurrentLinkedQueue[KeyPress]

  def start(configuration: Configuration, drawable: Drawable): Unit = {

    if (configuration.keyInputEnabled) {
      var oldKeysPressed = Set.empty[Int]

      DefaultTimer.fps(40) {
        DcsRemote.get("keyboard", maxAge = Some(Duration.fromMilliseconds(Int.MaxValue)))
          .map(JSON.readMap(_).keys.map(_.toInt).toSet)
          .map { keysPressed =>
            if (keysPressed != oldKeysPressed) {
              for (press <- keysPressed -- oldKeysPressed) {
                val event = KeyPress(press, keysPressed)
                inputQue.add(event)
              }
              drawable.draw()
            }
            oldKeysPressed = keysPressed
          }.onFailure {
          case e: Throttled => // Ignore ..
          case e: FailedFastException => // Ignore ..
          case e => logger.warning(s"Unable to read keyboard state from dcs remote: $e")
        }
      }
    }
  }
}

object Key {
  val SHIFT = 0x10
  val LSHIFT = 0xA0
  val RSHIFT = 0xA1
  val CONTROL = 0x11
  val LCONTROL = 0xA2
  val RCONTROL = 0xA3
  val ALT = 0x12
  val LALT = 164
  val LEFT = 0x25
  val RIGHT = 0x27
  val UP = 0x26
  val DOWN = 0x28
  val INSERT = 0x2D
  val DELETE = 0x2E
  val HOME = 0x24
  val END = 0x23
  val PAGE_UP = 0x21
  val PAGE_DOWN = 0x22
  val _0 = 48
  val _1 = 49
  val _2 = 50
  val _3 = 51
  val _4 = 52
  val _5 = 53
  val _6 = 54
  val _7 = 55
  val _8 = 56
  val _9 = 57
  val A = 65
  val B = 66
  val C = 67
  val D = 68
  val E = 69
  val F = 70
  val G = 71
  val H = 72
  val I = 73
  val J = 74
  val K = 75
  val L = 76
  val M = 77
  val N = 78
  val O = 79
  val P = 80
  val Q = 81
  val R = 82
  val S = 83
  val T = 84
  val U = 85
  val V = 86
  val W = 87
  val X = 88
  val Y = 89
  val Z = 90
  val F1 = 0x70
  val F2 = 0x71
  val F3 = 0x72
  val F4 = 0x73
  val F5 = 0x74
  val F6 = 0x75
  val F7 = 0x76
  val F8 = 0x77
  val F9 = 0x78
  val F10 = 0x79
  val F11 = 0x7A
  val F12 = 0x7B
  val F13 = 0x7C
  val F14 = 0x7D
  val F15 = 0x7E
  val F16 = 0x7F
  val F17 = 0x80
  val F18 = 0x81
  val F19 = 0x82
  val F20 = 0x83
  val F21 = 0x84
  val F22 = 0x85
  val F23 = 0x86
  val F24 = 0x87

  private val typ = scala.reflect.runtime.universe.typeOf[this.type]
  private val fieldsByName = typ.members
    .filterNot(_.isMethod)
    .map(_.asTerm)
    .map(t => t.name.toString.trim.toUpperCase -> t)
    .toMap

  private val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)

  def getVkode(name: String): Option[Int] = {

    val termSymbol =
      fieldsByName
        .get(name)
        .orElse(fieldsByName.get("_" + name))
    termSymbol.map(t => mirror.reflect(this).reflectField(t).get.asInstanceOf[Int])
  }
}

case class MouseClick(screenX: Int, screenY: Int, button: Int) {

  val hw = Gdx.graphics.getWidth.toFloat / 2.0f
  val hh = Gdx.graphics.getHeight.toFloat / 2.0f

  val x11Raw = (screenX.toFloat - hw) / hw
  val y11Raw = -(screenY.toFloat - hh) / hh
  val ortho11Raw = Vec2(x11Raw, y11Raw)

  val (sx, sy) =
    if (hw > hh) {
      (hw / hh, 1.0f)
    } else {
      (1.0f, hh / hw)
    }

  val x11 = sx * x11Raw
  val y11 = sy * y11Raw
  val ortho11 = Vec2(x11, y11)

  override def toString: String = {
    s"MouseClick[$x11, $y11][$x11Raw, $y11Raw]"
  }

}

case class KeyPress(key: Int, keysDown: Set[Int]) {
  def isKeyDown(key: Int): Boolean = keysDown.contains(key)
  def isShiftDown: Boolean = isKeyDown(Key.LSHIFT) || isKeyDown(Key.RSHIFT) || isKeyDown(Key.SHIFT)
  def isControlDown: Boolean = isKeyDown(Key.LCONTROL) || isKeyDown(Key.RCONTROL) || isKeyDown(Key.CONTROL)
  def isAltDown: Boolean = isKeyDown(Key.ALT) || isKeyDown(Key.LALT)
  override def toString: String = {
    val char = key.toString.toInt.toChar
    val base: String =
      if (KeyPress.modifiers.contains(key))
        ""
      else if (char.isLetterOrDigit)
        char.toString
      else
        key.toString
    s"${if (isControlDown) "Control " else ""}${if (isShiftDown) "Shift " else ""}${if (isAltDown) "Alt " else ""}$base"
  }
}

object KeyPress {
  import Key._
  val modifiers = Set(LSHIFT, RSHIFT, SHIFT, LCONTROL, RCONTROL, CONTROL, ALT)
}

case class Combination(key: Int, modifierTest: Combination.ModifierTest = _ => true) {
  def unapply(keyPress: KeyPress): Boolean = {
    modifierTest(keyPress) && keyPress.key == key
  }
}

object Combination {
  val UNBOUND = Combination(0, _ => false)
  type ModifierTest = KeyPress => Boolean
}
