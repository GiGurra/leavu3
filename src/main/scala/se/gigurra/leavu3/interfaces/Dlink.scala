package se.gigurra.leavu3.interfaces

import com.twitter.finagle.FailedFastException
import com.twitter.util.Duration
import com.github.gigurra.heisenberg.MapDataParser
import se.gigurra.leavu3.app.Version
import se.gigurra.leavu3.datamodel.{Configuration, DlinkConfiguration, DlinkData, Mark, Member, RawDlinkData}
import se.gigurra.leavu3.interfaces.DcsRemote.Stored
import se.gigurra.leavu3.util.{DefaultTimer, RestClient, Throttled}
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging
import com.github.gigurra.serviceutils.twitter.service.ServiceException

import scala.util.{Failure, Success, Try}

/**
  * Created by kjolh on 3/20/2016.
  */
object Dlink extends Logging {

  @volatile var config: DlinkConfiguration = DlinkConfiguration()
  @volatile var dlinkClient: Option[RestClient] = None
  @volatile var recvOk = false

  def connected: Boolean = dlinkClient.isDefined && recvOk

  def start(appCfg: Configuration): Unit = {
    In.start()
    Out.start(appCfg)
  }

  object CfgUpdate {
    def handleDlinkConfig(newConfig: DlinkConfiguration): Unit = {
      if (newConfig != config || dlinkClient.isEmpty) {
        logger.info(s"Updating dlink settings to: \n ${JSON.write(newConfig)}")
        config = newConfig
        In.clear()
        dlinkClient = Try(RestClient(newConfig.host, newConfig.port, "Data Link")).toOption
      }
    }
  }

  object In {

    @volatile var ownTeam: Map[String, DlinkData] = Map.empty
    @volatile var allTeams: Map[Int, Map[String, DlinkData]] = Map.empty

    def notPlaying: Map[String, DlinkData] = allTeams.getOrElse(0, Map.empty)
    def blue: Map[String, DlinkData] = allTeams.getOrElse(2, Map.empty)
    def red: Map[String, DlinkData] = allTeams.getOrElse(1, Map.empty)

    def clear(): Unit = {
      ownTeam = Map.empty
      allTeams = Map.empty
    }

    def doMasterUpdate(): Unit = {
      dlinkClient.foreach(_
        .get(config.team, maxAge = Some(Duration.fromSeconds(6)))
        .foreach { rawData =>
          val mapData = JSON.readMap(rawData)
          processDlinkMapData(mapData)
          DcsRemote.store("dlink-in", "all", rawData) // Store for slaves to use
        }
        .onFailure {
          case e: Throttled =>
          case e: ServiceException =>
            recvOk = false
            clear()
            logger.error(s"Data link host replied with an error: $e")
          case e: FailedFastException =>
            recvOk = false
            clear()
          case e =>
            recvOk = false
            clear()
            logger.error(e, s"Unexpected error when attempting to receive data link")
        })
    }

    def processDlinkMapData(mapData: Map[String, Any]): Unit = {
      val everyoneOnNetwork = membersByName(mapData)
      allTeams = everyoneOnNetwork.groupBy(_._2.selfData.coalitionId)
      ownTeam = allTeams.getOrElse(GameIn.snapshot.selfData.coalitionId, Map.empty)
      recvOk = true
    }

    def doSlaveUpdate(): Unit = {
      DcsRemote.loadFromSource[RawDlinkData](category = "dlink-in", maxAge = Some(Duration.fromSeconds(3))).map(_.get("all") match {
        case None => recvOk = false
        case Some(data) => processDlinkMapData(data.item.source)
      })
    }

    def start(): Unit = {
      DefaultTimer.fps(1) {
        CfgUpdate.handleDlinkConfig(DcsRemote.remoteConfig.dlinkSettings)
        if (DcsRemote.isActingMaster) {
          doMasterUpdate()
        }
      }
      DefaultTimer.fps(10) {
        if (DcsRemote.isActingSlave) {
          doSlaveUpdate()
        }
      }
    }

    def membersByName(data: Map[String, Any]): Map[String, DlinkData] = {
      data.collect { case ValidDlinkData(id, dlinkData) => id -> dlinkData }
    }

    object ValidDlinkData {
      def unapply(callsignAndData: (String, Any)): Option[(String, DlinkData)] = {
        val callsign = callsignAndData._1
        val raw = callsignAndData._2
        Try(implicitly[MapDataParser[DlinkData]].parse(raw)) match {
          case Success(dlinkData) => Some(callsign, dlinkData)
          case Failure(e) =>
            logger.warning(s"Failed to read dlink data from $callsign")
            None
        }
      }
    }

    def sameTeam(data: DlinkData): Boolean = {
      data.selfData.coalitionId == GameIn.snapshot.selfData.coalitionId
    }
  }


  object Out extends Logging {

    def marks: Map[String, Stored[Mark]] = DcsRemote.loadFromCache[Mark]("marks", maxAge = Some(Duration.fromMilliseconds(Int.MaxValue)), minTimeDelta = Some(Duration.fromMilliseconds(100)))
    def hasMark(id: String): Boolean = marks.contains(id)
    def addMark(id: String, mark: Mark): Unit = DcsRemote.store("marks", id, mark)
    def deleteMark(id: String): Unit = DcsRemote.delete("marks", id)

    def start(appCfg: Configuration): Unit = {

      DefaultTimer.fps(2) {

        val source = GameIn.snapshot
        if (DcsRemote.isActingMaster && source.err.isEmpty && source.age < 3.0) {

          dlinkClient.foreach {
            _.put(s"${config.team}/${config.callsign}") {

              val self = Member.marshal(
                Member.planeId -> source.metaData.planeId,
                Member.modelTime -> source.metaData.modelTime,
                Member.position -> source.selfData.position,
                Member.velocity -> source.flightModel.velocity,
                Member.targets -> source.sensors.targets.locked,
                Member.selfData -> source.metaData.selfData,
                Member.markPos -> marks.mapValues(_.item),
                Member.pylons -> source.payload.toDlinkPayload(appCfg),
                Member.mode -> source.aircraftMode,
                Member.sensor -> source.sensors.status,
                Member.leavuVersion -> Version.currentOpt
              )
              JSON.write(self)
            }.onFailure {
              case e: Throttled => // ignore
              case e: FailedFastException => // ignore
              case e: ServiceException => logger.error(s"Data link host replied with an error: $e")
              case e => logger.error(e, s"Unexpected error when attempting to send to dlink")
            }
          }
        }
      }
    }
  }

}
