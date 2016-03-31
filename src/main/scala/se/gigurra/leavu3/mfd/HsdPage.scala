package se.gigurra.leavu3.mfd

import com.badlogic.gdx.graphics.Color
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.Projection
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.{CircleBuffer, CurTime}
import se.gigurra.leavu3.interfaces.{Dlink, MouseClick}

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by kjolh on 3/12/2016.
  */
case class HsdPage(implicit config: Configuration) extends Page("HSD") {

  var shouldMatchIngameScale = true
  var shouldDrawDetailedHsi = true
  var shouldDrawOwnHeading = true
  val deprFactor = CircleBuffer(0.0, 0.5).withDefaultValue(0.5)

  val OSB_DEPR = 1
  val OSB_HDG = 2
  val OSB_SCALE = 17
  val OSB_HSI = 3
  val OSB_DEL = 7
  val OSB_UNITS = 9

  override def pressOsb(i: Int): Unit = {
    i match {
      case OSB_DEPR => deprFactor.stepDown()
      case OSB_HSI => shouldDrawDetailedHsi = !shouldDrawDetailedHsi
      case OSB_HDG => shouldDrawOwnHeading = !shouldDrawOwnHeading
      case OSB_DEL => Dlink.Out.deleteMark(Dlink.config.callsign)
      case OSB_UNITS => displayUnits.stepUp()
      case _ => // Nothing yet
    }
  }

  override def mouseClicked(click: MouseClick): Unit =  {
    val screenCenter = Vec2(0.0, -deprFactor)
    val offs = (click.ortho11 - screenCenter) * distScale
    val relativeBra = offs.asBra
    val bra = relativeBra.copy(bearingRaw = self.heading + relativeBra.bearing)
    val clickPos = self.position + bra.toOffset
    Dlink.Out.addMark(Mark(Dlink.config.callsign, clickPos))
  }

  override def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    matchIngameScale(game)
    viewport(viewportSize = distScale * 2.0, offs = Vec2(0.0, -distScale * deprFactor), heading = self.heading){
      implicit val p = ppiProjection
      drawSelf(game)
      drawHsi(game)
      drawWayPoints(game)
      drawDlinkMarks(dlinkIn)
      drawScanZone(game)
      drawAiWingmen(game)
      drawAiWingmenTargets(game)
      drawDlinkMembersAndTargets(dlinkIn)
      drawLockedTargets(game)
      drawTdc(game)
    }(ppiProjection)
    drawBullsEyeNumbers(game)
    drawBraNumbers(game)
    drawOwnHeading(game)
    drawModes(game)
    drawOsbs(game)
  }

  def matchIngameScale(game: GameData) = {
    if (shouldMatchIngameScale) {
      val x = distScale.items.minBy(x => math.abs(x - game.sensors.status.scale.distance))
      distScale.set(x)
    }
  }

  def drawHsi(game: GameData): Unit = {
    drawHsi(close = true, middle = true, far = true, tics = shouldDrawDetailedHsi)(ppiProjection)
  }

  def drawSelf(game: GameData): Unit = {
    drawSelf(0.0)(ppiProjection)
  }

  def drawWayPoints(game: GameData): Unit = {
    implicit val p = ppiProjection

    def wpByIndex(i: Int): Option[Waypoint] = {
      game.route.waypoints.find(_.index == i)
    }

    for (wp <- game.route.waypoints) {
      wpByIndex(wp.next) foreach { nextWp =>
        val thisOne = wp.position - self.position
        val nextOne = nextWp.position - self.position
        lines(Seq(thisOne -> nextOne), WHITE)
      }
      drawWpByIndex(wp, wp.index == game.route.currentWaypoint.index)
    }
  }

  def drawScanZone(game: GameData): Unit = {
    implicit val p = ppiProjection
    val sensors = game.sensors.status
    if (sensors.sensorOn) {
      val dist = sensors.scale.distance
      val (direction, width) = scanZoneAzDirectionAndWidth
      arc(radius = dist, angle = width, direction = direction, color = LIGHT_GRAY)
    }
  }

  def drawLockedTargets(game: GameData): Unit = {
    implicit val p = ppiProjection

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index

    implicit class ContactWithIndex(c: Contact) {
      def index: Int = order(c.id)
    }

    import game.sensors.targets._
    for {
      collection <- Seq(detected, tws.map(_.contact), locked.map(_.contact))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
    }

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    for (contact <- bearingsDesignated) {
      val offs = contact.position - self.position : Vec2
      lines(Seq(Vec2() -> offs) * 10000.0, YELLOW)
    }

    for (contact <- positionsDesignated.reverse) { // draw lowest index (=highest prio) last
      drawContact(
        position = contact.position,
        heading = Some(contact.heading),
        color = contactColor(contact, fromDatalink = false),
        centerText = (contact.index + 1).toString,
        fill = true
      )
    }
  }

  def drawBullsEyeNumbers(game: GameData) = {
    implicit val p = screenProjection

    def mkBraString(prefix: String, bra: Bra): String = s"$prefix : ${bra.brString(displayUnits.m_to_distUnit)}"

    val bullsEye = game.route.currentWaypoint
    val selfBra = (self.position - bullsEye.position).asBra
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { at((-0.9, 0.9)) {

      transform(_
        .scalexy(scale)) {

        val beStr = s"bullseye : wp ${bullsEye.index - 1}"
        val selfStr = mkBraString("self".pad(8), selfBra)

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        drawTextLine(beStr, LIGHT_GRAY)
        drawTextLine(selfStr, CYAN)

        game.tdcPosition foreach { tdc =>
          val tdcBra = (tdc - bullsEye.position).asBra
          val tdcStr = mkBraString("tdc".pad(8), tdcBra)
          drawTextLine(tdcStr, WHITE)
        }

        game.pdt.filter(_.isPositionKnown) foreach { pdt =>
          val pdtBra = (pdt.position - bullsEye.position).asBra
          val pdtStr = mkBraString("pdt".pad(8), pdtBra)
          drawTextLine(pdtStr, contactColor(pdt, fromDatalink = false))
        }

      }
    }}

  }

  def drawBraNumbers(game: GameData) = {
    implicit val p = screenProjection

    def mkBraString(prefix: String, bra: Bra): String = s"$prefix : ${bra.brString(displayUnits.m_to_distUnit)}"

    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched { at((0.45, 0.9)) {

      transform(_
        .scalexy(scale)) {

        val beStr = s" BR from : self"

        var n = 0
        def drawTextLine(str: String, color: Color): Unit = {
          transform(_.translate(y = -n.toFloat * font.getLineHeight))(str.drawRaw(xAlign = 0.5f, color = color))
          n += 1
        }

        drawTextLine(beStr, LIGHT_GRAY)

        val wp = game.route.currentWaypoint
        val wpBra = (wp.position - self.position).asBra
        val wpStr = mkBraString(s"wp ${wp.index-1}".pad(8), wpBra)
        drawTextLine(wpStr, DARK_GRAY)

        game.tdcPosition foreach { tdc =>
          val tdcBra = (tdc - self.position).asBra
          val tdcStr = mkBraString("tdc".pad(8), tdcBra)
          drawTextLine(tdcStr, WHITE)
        }

        game.pdt.filter(_.isPositionKnown) foreach { pdt =>
          val pdtBra = (pdt.position - self.position).asBra
          val pdtStr = mkBraString("pdt".pad(8), pdtBra)
          drawTextLine(pdtStr, contactColor(pdt, fromDatalink = false))
        }

      }
    }}

  }

  def drawOsbs(game: GameData): Unit = {
    implicit val p = screenProjection
    import Mfd.Osb._
    drawBoxed(OSB_DEPR, "DEP", boxed = deprFactor.index != 0)
    drawBoxed(OSB_HDG, "HDG", boxed = shouldDrawOwnHeading)
    drawBoxed(OSB_HSI, "HSI", boxed = shouldDrawDetailedHsi)
    if (Dlink.Out.hasMark(Dlink.config.callsign))
      drawBoxed(OSB_DEL, "DEL", boxed = false)
    drawBoxed(OSB_SCALE, (distScale.get * displayUnits.m_to_distUnit).round.toString, boxed = false)
    Mfd.Osb.draw(OSB_UNITS, displayUnits.name.toUpperCase.take(3))
  }

  def drawOwnHeading(game: GameData): Unit = {
    implicit val p = screenProjection
    if (shouldDrawOwnHeading) {
      batched {
        at(Mfd.Osb.positions(2) - Vec2(0.0, 0.1)) {
          self.heading.round.toString.pad(3, '0').drawCentered(WHITE)
        }
      }
    }
  }

  def drawModes(game: GameData): Unit = {
    implicit val p = screenProjection
    val sensors = game.sensors.status
    val scale = config.symbolScale * 0.02 / font.getSpaceWidth

    batched {
      at((-0.9, 0.65)) {

        transform(_
          .scalexy(scale)) {

          var n = 0
          def drawTextLine(value: Any, color: Color): Unit = {
            transform(_.translate(y = -n.toFloat * font.getLineHeight)) {
              value.toString.drawRaw(xAlign = 0.5f, color = color)
            }
            n += 1
          }

          drawTextLine(s"${game.aircraftMode.master} / ${game.aircraftMode.submode}", LIGHT_GRAY)
          drawTextLine(s"${sensors.prf.selection} / ${sensors.prf.current}", LIGHT_GRAY)
        }
      }
    }
  }

}
