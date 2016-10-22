package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.finagle.http.Status
import com.twitter.util.{Duration, Future}
import se.gigurra.leavu3.datamodel._
import se.gigurra.leavu3.gfx.Drawable
import se.gigurra.leavu3.util._
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging
import com.github.gigurra.serviceutils.twitter.service.ServiceException

import scala.collection.mutable

/**
  * Created by kjolh on 3/20/2016.
  */
object GameIn extends Logging {

  val path = "export/dcs_remote_export_data()"
  val exportFunctionVersion = 1
  val versionFunctionName = "dcs_remote_export_version"
  val versionFunctionPath = s"export/$versionFunctionName()"
  val luaDataExportScript = Resource2String("lua_scripts/LoDataExport.lua")
  val versionFunctionSourceCode: String = {
    s"""
       |function $versionFunctionName()
       |  return { version = $exportFunctionVersion }
       |end
       """.stripMargin
  }

  @volatile var snapshot = GameData()
  @volatile var dcsRemoteConnected = true
  @volatile var dcsGameConnected = false

  def start(appCfg: Configuration, drawable: Drawable): Unit = {
    ScriptInject.start(appCfg)
    Updater.start(appCfg, drawable)
  }

  object Updater {

    // Try having the update timer on its own thread
    object updateTimer extends UtilTimer

    def start(appCfg: Configuration, drawable: Drawable): Unit = {
      updateTimer.fps(appCfg.gameDataFps) {
        DcsRemote
          .get(path, Some(Duration.fromSeconds(if (DcsRemote.isActingMaster) 0 else 1))) // Master will update
          .map(JSON.read[GameDataWire])
          .map(_.toGameData)
          .map(postProcess)
          .map { newSnapshot =>
            snapshot = newSnapshot
            dcsRemoteConnected = true
            dcsGameConnected = true
            drawable.draw()
          }.onFailure {
          case e: Throttled => // Ignore
          case e: ServiceException =>
            e.response.status match {
              case Status.ServiceUnavailable => // No need to log every message when not having dcs up and running
              case _ => logger.warning(s"Dcs Remote replied: Could not fetch game data from Dcs Remote: $e")
            }
            dcsRemoteConnected = true
            dcsGameConnected = false
            snapshot = GameData()
          case e: FailedFastException =>
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
          case e =>
            logger.error(s"Could not fetch game data from Dcs Remote: $e")
            dcsRemoteConnected = false
            dcsGameConnected = false
            snapshot = GameData()
        }
      }
    }
  }

  private val knownNavWaypoints = new mutable.HashMap[Int, Waypoint]
  private val rdrMemory = ContactMemory()
  private val rdrPositionUpdateMemory = new PositionChangeMemory()
  private val fuelConsumption = DerivativeAverager(overSeconds = 0.75, minTimeStep = 0.25)

  @volatile var wingmenTgtsLastTminus1 = Seq.empty[Vec3] // To calculate ai wingmen tgt headings
  @volatile var wingmenTgtsLastTminus2 = Seq.empty[Vec3] // To calculate ai wingmen tgt headings

  def estimatedFueldConsumption: Double = fuelConsumption.get

  def rdrMemory(contact: Contact): Option[Memorized[Contact]] = {
    rdrMemory.get(contact)
  }

  def rdrPositionMemory(contact: Contact): Option[Memorized[Contact]] = {
    rdrPositionUpdateMemory.get(contact)
  }

  private def postProcess(newData: GameData): GameData = {
    newData
      .waypointWorkaround()
      .rdrMemoryWorkaround()
      .aiWingmenTgtHeadingsWorkaround()
      .fuelConsumptionWorkaround()
  }

  implicit class GameDataWorkarounds(newData: GameData) {

    def fuelConsumptionWorkaround(): GameData = {
      fuelConsumption.update(-newData.indicators.engines.fuelTotal, newData.metaData.modelTime)
      newData
    }

    def waypointWorkaround(): GameData = {
      if (newData.route.waypoints.nonEmpty) {
        newData
      } else {
        // DCS FC waypoint export in NAV mode is broken.. we only get the current one.
        // so keep in memory which ones we've cycled through (yay - Nice idea by GG!)
        val curWp = newData.route.currentWaypoint
        knownNavWaypoints.get(curWp.index) match {
          case Some(knownWaypoint) if knownWaypoint != curWp =>
            logger.info(s"Known navpoint changed - assuming mission change - clearing known navpoints!")
            knownNavWaypoints.clear()
          case _ =>
          // No clearing required. Same or new waypoint
        }

        // DCS gives us an extra wp (index 2 million :))
        if (0 <= curWp.index && curWp.index < 100)
          knownNavWaypoints.put(curWp.index, curWp)
        else
          knownNavWaypoints.put(-1, curWp.withIndex(-1))

        val allWps = knownNavWaypoints.values.toSeq.sortBy(_.index)
        val newRoute = newData.route.withWaypoints(allWps)
        newData.withRoute(newRoute)
      }
    }

    def rdrMemoryWorkaround(): GameData = {
      rdrMemory.update(newData.sensors.targets.all)
      // Cannot do for detected contacts since they are broken for TWS in the DCS export
      rdrPositionUpdateMemory.update((newData.sensors.targets.tws ++ newData.sensors.targets.locked).map(_.contact))
      val rwsContactsKnown = rdrMemory.all.filter(_.isRws)
      newData.withRwsMemory(rwsContactsKnown.map(_.t)).withoutHiddenContacts
    }

    def aiWingmenTgtHeadingsWorkaround(): GameData = {
      if (wingmenTgtsLastTminus1 != newData.aiWingmenTgts) {
        wingmenTgtsLastTminus2 = wingmenTgtsLastTminus1
        wingmenTgtsLastTminus1 = newData.aiWingmenTgts
      }
      newData
    }

  }

  object ScriptInject extends Logging {

    def start(appCfg: Configuration): Unit = {

      def doInject(): Future[Unit] = {
        DcsRemote.post("export")(luaDataExportScript).flatMap( _ => DcsRemote.post("export")(versionFunctionSourceCode))
      }

      DefaultTimer.fps(1) {
        if (DcsRemote.isActingMaster) {
          DcsRemote.get(versionFunctionPath).map(JSON.read[ExportVersion]).flatMap {
            case data if data.err.isDefined =>
              logger.info(s"Injecting data export script (None present) .. -> ${GameIn.path}")
              doInject()
            case data if data.version < exportFunctionVersion && data.version != -1 =>
              logger.info(s"Injecting data export script (New script version) .. -> ${GameIn.path}")
              doInject()
            case _ =>
              Future.Unit // Good to go!
          }.onFailure {
            case e: Throttled => // Ignore ..
            case e: FailedFastException => // Ignore ..
            case e: ServiceException => e.response.status match {
              case Status.ServiceUnavailable => // No need to log every message when not having dcs up and running
              case _ => logger.warning(s"Dcs Remote replied: Unable to inject game export script: $e")
            }
            case e => logger.error(e, s"Unable to inject export script")
          }
        }
      }
    }
  }

}
