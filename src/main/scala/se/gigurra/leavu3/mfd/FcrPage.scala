package se.gigurra.leavu3.mfd

import se.gigurra.leavu3.datamodel.{Bra, Configuration, Contact, DlinkData, GameData, Vec2, self}
import se.gigurra.leavu3.gfx.{BScopeProjection, Projection}
import se.gigurra.leavu3.interfaces.GameIn
import se.gigurra.leavu3.gfx.RenderContext._
import se.gigurra.leavu3.util.CurTime

import scala.collection.mutable

/**
  * Created by kjolh on 3/12/2016.
  */
case class FcrPage(implicit config: Configuration) extends Page("FCR") {

  def screenDistMeters: Double = GameIn.snapshot.sensors.status.scale.distance
  def screenWidthDegrees: Double = GameIn.snapshot.sensors.status.scale.azimuth
  val inset = 0.2

  def draw(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    drawConformal(game, dlinkIn)
    drawInfo(game, dlinkIn)
  }

  def drawConformal(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val bscopeProjection = BScopeProjection(screenWidthDegrees, config.use3dBscope)
    transform(_.scalexy(1.0 - inset)) {

      viewport(screenDistMeters, self.heading, offs = Vec2(0.0, 0.0)) {
        scissor(at = (0.0, 0.0), size = (screenDistMeters, screenDistMeters)) {
          drawScanZoneUnderlay(game)
          drawSelectedWaypoint(game)
          drawDlinkMarks(dlinkIn)
          drawAiWingmen(game)
          drawAiWingmenTargets(game)
          drawDlinkMembersAndTargets(dlinkIn)
          drawOwnContacts(game, dlinkIn)
          drawScanZoneOverlay(game)
          drawTdc(game)
        }
        drawSurroundEdge()
      }
    }
  }

  def drawScanZoneUnderlay[_: Projection](game: GameData): Unit = {

    def drawGreyUnderlay(): Unit = {
      val braMiddleScreen = Bra(self.heading, screenDistMeters*0.5, 0.0)
      at(self.position + braMiddleScreen.toOffset : Vec2, heading = self.heading) {
        rect(screenDistMeters, screenDistMeters, typ = FILL, color = DARK_GRAY.scaleAlpha(0.25f))
      }
    }

    def drawScannedArea(): Unit = {
      if (game.sensors.status.sensorOn) {
        val (direction, width) = scanZoneAzDirectionAndWidth

        val braScanCenter = Bra(direction, screenDistMeters * 0.5, 0.0)
        val hCoverage = width / screenWidthDegrees

        at(self.position + braScanCenter.toOffset: Vec2, heading = self.heading) {
          rect(hCoverage * screenDistMeters, screenDistMeters, typ = FILL, color = BLACK)
        }
      }
    }

    drawGreyUnderlay()
    drawScannedArea()
  }


  def drawSelectedWaypoint[_: Projection](game: GameData): Unit = {
    drawWp(game.route.currentWaypoint, None, selected = true)
  }

  def drawScanZoneOverlay[_: Projection](game: GameData): Unit = {

  }

  def drawOwnContacts[_: Projection](game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {

    val contacts = new mutable.HashMap[Int, Contact] // id -> data
    val order = new mutable.HashMap[Int, Int] // id -> index
    val rws = new mutable.HashMap[Int, Boolean] // id -> index

    import game.sensors.targets._

    for {
      (collection, isRws) <- Seq((detected, true), (tws.map(_.contact), false), (locked.map(_.contact), false))
      (contact, i) <- collection.zipWithIndex
    } {
      order.put(contact.id, i)
      contacts.put(contact.id, contact)
      rws.put(contact.id, isRws)
    }

    implicit class RichContact(c: Contact) {
      def index: Int = order(c.id)
      def isRws: Boolean = rws(c.id)
      def news: Double = GameIn.rdrMemory(c).fold(1.0)(_.news)
    }

    val positionsEchoed = contacts.values.toSeq
      .filterNot(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val positionsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filter(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsEchoed = contacts.values.toSeq
      .filterNot(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    val bearingsDesignated = contacts.values.toSeq
      .filter(_.isDesignated)
      .filterNot(_.isPositionKnown)
      .sortBy(_.index)

    def drawKnownPosContacts(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) { // draw lowest index (=highest prio) last

        val baseColor = contactColor(contact, fromDatalink = false)
        val color = baseColor.scaleAlpha(contact.news)
        val lag = if (contact.isDesignated || contact.isRws) 0.0 else GameIn.rdrLastTwsPositionUpdate(contact).fold(0.0)(CurTime.seconds - _.timestamp)
        val position = contact.position + contact.velocity * lag

        drawContact(
          position = position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = color,
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          fill = contact.isDesignated,
          drawAlt = !contact.isRws
        )
      }
    }

    def drawJammers(contacts: Seq[Contact]): Unit = {
      for (contact <- contacts.reverse) { // draw lowest index (=highest prio) last

        val baseColor = YELLOW
        val color = baseColor.scaleAlpha(contact.news)

        if (contact.isDesignated) {
          lineBetween(0.5 * (self.position + contact.position), contact.position, YELLOW, scaleIn = 10000.0, scaleOut = 10000.0)
        }

        drawJammer(
          position_actual = contact.position,
          heading = if (contact.isRws) None else Some(contact.heading),
          color = color,
          centerText = if (contact.isDesignated) (contact.index + 1).toString else "",
          designated = contact.isDesignated,
          drawDistUndesignated = math.min(math.max(30.nmi, screenDistMeters * 0.75), screenDistMeters * 0.95)
        )

      }
    }

    // Separated to ensure draw order
    drawJammers(bearingsEchoed)
    drawJammers(bearingsDesignated)

    // Separated to ensure draw order
    drawKnownPosContacts(positionsEchoed)
    drawKnownPosContacts(positionsDesignated)

  }

  def drawSurroundEdge[_: Projection](): Unit = {
    rect(screenDistMeters, screenDistMeters, color = TEAL)
  }

  def drawInfo(game: GameData, dlinkIn: Seq[(String, DlinkData)]): Unit = {
    implicit val p = screenProjection
    drawBearings(game)
    drawElevations(game)
    drawBullsEyeNumbers(game)
    drawBraNumbers(game)
    drawOwnHeading(game)
    drawModes(game)
    drawDlzs(game)
    drawTargetInfo(game)
    drawOsbs(game)
  }

  def drawBearings[_: Projection](game: GameData): Unit = {
  }

  def drawElevations[_: Projection](game: GameData): Unit = {
  }

  def drawBullsEyeNumbers[_: Projection](game: GameData): Unit = {
  }

  def drawBraNumbers[_: Projection](game: GameData): Unit = {
  }

  def drawOwnHeading[_: Projection](game: GameData): Unit = {
  }

  def drawModes[_: Projection](game: GameData): Unit = {
  }

  def drawOsbs[_: Projection](game: GameData): Unit = {
  }

  def drawDlzs[_: Projection](game: GameData): Unit = {
  }

  def drawTargetInfo[_: Projection](game: GameData): Unit = {
  }

}
