package se.gigurra.leavu3.windowstweaks

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import se.gigurra.leavu3.datamodel.Configuration
import se.gigurra.leavu3.util.JavaReflectImplicits
import se.gigurra.serviceutils.twitter.logging.Logging


/**
  * Created by kjolh on 3/17/2016.
  */
object WindowTweaks extends Logging with JavaReflectImplicits {

  val displayClass = Class.forName("org.lwjgl.opengl.Display")
  val display = displayClass.reflectField("display_impl")

  var extraWidth: Int = 16
  // Default on windows
  var extraHeight: Int = 39 // Default on windows

  val SWP_NOMOVE = 0x0002
  val SWP_NOSIZE = 0x0001
  val SWP_NOZORDER = 0x0004
  val GWL_EXSTYLE = -20
  val WS_EX_NOACTIVATE = 0x08000000L
  val SWP_FRAMECHANGED = 0x0020L
  val HWND_TOPMOST = -1L
  val HWND_BOTTOM = 1L

  // Ugly but required State
  var windowStealsFocus = true

  case class Rect(x: Int, y: Int, width: Int, height: Int)

  def apply(configuration: Configuration): Unit = {

    if (configuration.borderless) {
      extraWidth = 0
      extraHeight = 0
    }

    if (configuration.noFocusOnClick)
      setNeverCaptureFocus()

    if (configuration.alwaysOnTop)
      setAlwaysOnTop()
  }

  def isPlatformWindows: Boolean = {
    display.getClass.getName == "org.lwjgl.opengl.WindowsDisplay"
  }

  def getWindowPosition: Rect = {
    Rect(
      display.reflectGetter("getX").asInstanceOf[Int],
      display.reflectGetter("getY").asInstanceOf[Int],
      display.reflectGetter("getWidth").asInstanceOf[Int] + extraWidth,
      display.reflectGetter("getHeight").asInstanceOf[Int] + extraHeight
    )
  }


  def updateStateMachine(config: Configuration): Unit = {

    if (isPlatformWindows && config.noFocusOnClick) {

      def windowHasFocus: Boolean = {
        val hwndLong = display.reflectField("hwnd").asInstanceOf[Long]
        val hwnd = new HWND(new Pointer(hwndLong))
        val hwndWithFocus = AwGetter.INSTANCE.GetActiveWindow()
        hwnd == hwndWithFocus
      }

      def windowIsForeground: Boolean = {
        val hwndLong = display.reflectField("hwnd").asInstanceOf[Long]
        val hwnd = new HWND(new Pointer(hwndLong))
        val hwndWithFocus = User32.INSTANCE.GetForegroundWindow()
        hwnd == hwndWithFocus
      }

      if (windowHasFocus && windowIsForeground)
        windowStealsFocus = true

      if (windowStealsFocus && !windowHasFocus) {
        setNeverCaptureFocus()
        windowStealsFocus = false
      }
    }
  }

  def setNeverCaptureFocus(on: Boolean = true): Unit = {

    if (isPlatformWindows) {

      logger.info(s"Setting up WS_EX_NOACTIVATE")

      val hwndLong = display.reflectField("hwnd").asInstanceOf[Long]

      def setWindowLong(index: Int, value: Long): Unit = display.reflectInvoke("setWindowLongPtr", hwndLong: java.lang.Long, index: java.lang.Integer, value: java.lang.Long) //setWindowLongMethod.invoke(display, hwnd: java.lang.Long, index : java.lang.Integer, value : java.lang.Long)
      def getWindowLong(index: Int): Long = display.reflectInvoke("getWindowLongPtr", hwndLong: java.lang.Long, index: java.lang.Integer).asInstanceOf[Long]

      val prevStyle = getWindowLong(GWL_EXSTYLE)
      val withStyle = prevStyle | WS_EX_NOACTIVATE
      val withoutStyle = withStyle - WS_EX_NOACTIVATE

      setWindowLong(GWL_EXSTYLE, if (on) withStyle else withoutStyle)

      // See https://msdn.microsoft.com/en-us/library/windows/desktop/ms633545(v=vs.85).aspx
      // for why this is required.
      display.reflectInvoke(
        "setWindowPos",
        hwndLong: java.lang.Long,
        0L: java.lang.Long,
        0: java.lang.Integer,
        0: java.lang.Integer,
        0: java.lang.Integer,
        0: java.lang.Integer,
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOZORDER | SWP_FRAMECHANGED: java.lang.Long
      )

    } else {
      logger.warning(s"Setting configuration.noFocusOnClick unavailable on this operating system!")
    }
  }

  def setAlwaysOnTop(on: Boolean = true): Unit = {

    if (isPlatformWindows) {

      val hwnd = display.reflectField("hwnd").asInstanceOf[Long]
      val oldWindowPos = getWindowPosition

      def invoke() = display.reflectInvoke(
        "setWindowPos",
        hwnd: java.lang.Long,
        (if (on) HWND_TOPMOST else HWND_BOTTOM): java.lang.Long,
        oldWindowPos.x: java.lang.Integer,
        oldWindowPos.y: java.lang.Integer,
        oldWindowPos.width: java.lang.Integer,
        oldWindowPos.height: java.lang.Integer,
        SWP_FRAMECHANGED: java.lang.Long
      )

      invoke()

      // Ensure that the window actually gets the desired size, otherwise, adjust
      val newWindowPos = getWindowPosition
      if (newWindowPos != oldWindowPos) {
        extraWidth += oldWindowPos.width - newWindowPos.width
        extraHeight += oldWindowPos.height - newWindowPos.height
        invoke()
      }

    } else {
      logger.warning(s"Setting configuration.alwaysOnTop unavailable on this operating system!")
    }
  }

}
