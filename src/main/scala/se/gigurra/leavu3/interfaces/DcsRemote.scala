package se.gigurra.leavu3.interfaces

import java.util.UUID

import com.twitter.finagle.FailedFastException
import com.twitter.util.{Await, Duration, Future}
import com.github.gigurra.heisenberg.MapData.SourceData
import com.github.gigurra.heisenberg._
import se.gigurra.leavu3.datamodel.DlinkData._
import se.gigurra.leavu3.datamodel.{Configuration, DcsRemoteRemoteConfig, Leavu3Instance}
import se.gigurra.leavu3.util.{DefaultTimer, Throttled, RestClient}
import com.github.gigurra.serviceutils.json.JSON
import com.github.gigurra.serviceutils.twitter.logging.Logging

import scala.language.implicitConversions
import scala.util.control.NonFatal

case class DcsRemote private(config: Configuration) extends Logging {
  import DcsRemote._

  private val forcedSlave = config.forcedSlave
  private val ownInstanceId = UUID.randomUUID().toString
  private val client = RestClient(config.dcsRemoteAddress, config.dcsRemotePort, "Dcs Remote")
  private val cache = new scala.collection.concurrent.TrieMap[String, Map[String, Stored[_]]]
  @volatile var remoteConfig = initialDownloadConfig()
  @volatile var ownPriority: Int = 0

  DefaultTimer.fps(1) {
    downloadUpdatedConfig()
      .onSuccess { staticData =>
        remoteConfig = staticData
      }
      .onFailure {
        case e: Throttled =>
        case e: FailedFastException =>
        case e => logger.warning(s"Unable to download configuration from Dcs Remote: $e")
      }
  }

  DefaultTimer.fps(10) {
    registerLeavu3Instance()
      .onFailure {
        case e: Throttled =>
        case e: FailedFastException =>
        case e => logger.warning(s"Unable to register Leavu3 instance on Dcs Remote: $e")
      }
  }

  def store[T: MapProducer](category: String, id: String, data: => T): Future[Unit] = {
    client.put(s"$category/$id")(JSON.write(data))
  }

  def store(category: String, id: String, data: => String): Future[Unit] = {
    client.put(s"$category/$id")(data)
  }

  def delete(category: String, id: String): Future[Unit] = {
    client.delete(s"$category/$id")
  }

  def loadFromSource[T: MapParser](category: String, maxAge: Option[Duration], minTimeDelta: Option[Duration] = None): Future[Map[String, Stored[T]]] = {
    client.get(category, maxAge = maxAge, minTimeDelta = minTimeDelta).map { data =>
      val out = JSON.readMap(data).asInstanceOf[Map[String, SourceData]].map {
        case (k, v) => k ->
          Stored[T](
            timestamp = v("timestamp").asInstanceOf[Double],
            age = v("age").asInstanceOf[Double],
            item = MapParser.parse[T](v("data").asInstanceOf[Map[String, SourceData]])
          )
      }
      cache.put(category, out)
      out
    }.onFailure {
      case e: Throttled =>
      case e: FailedFastException =>
      case NonFatal(e) => logger.warning(s"Unable to download and process category $category from local Dcs Remote: $e")
    }.rescue {
      case NonFatal(e) => Future.value(getCached(category))
    }
  }

  /**
    * Load will always be empty on the first attempt,
    * since it triggers the actual download from the Dcs Remote
    */
  def loadFromCache[T: MapParser](category: String, maxAge: Option[Duration], minTimeDelta: Option[Duration] = None): Map[String, Stored[T]] = {
    loadFromSource[T](category, maxAge, minTimeDelta)
    getCached(category)
  }

  private def getCached[T](category: String): Map[String, Stored[T]] = {
    cache.get(category).map(_.asInstanceOf[Map[String, Stored[T]]]).getOrElse(Map.empty[String, Stored[T]])
  }

  private def initialDownloadConfig(): DcsRemoteRemoteConfig = {
    try Await.result(downloadUpdatedConfig()) catch {
      case NonFatal(e) => throw new RuntimeException(s"Failed to communicate with dcs remote!", e)
    }
  }

  private def downloadUpdatedConfig(): Future[DcsRemoteRemoteConfig] = {
    client.get(s"static-data").map(JSON.read[DcsRemoteRemoteConfig])
  }

  private def registerLeavu3Instance(): Future[Unit] = {
    store("leavu3-instances", ownInstanceId, Leavu3Instance(ownInstanceId, if (forcedSlave) -1 else ownPriority, isActingMaster))
  }

  def isActingSlave: Boolean = {
    !isActingMaster
  }

  def isActingMaster: Boolean = {

    if (forcedSlave) {
      false
    }

    else {

      val instanceLkup = loadFromCache[Leavu3Instance]("leavu3-instances", maxAge = Some(Duration.fromSeconds(1)), minTimeDelta = Some(Duration.fromMilliseconds(20)))

      instanceLkup.get(ownInstanceId) match {
        case None => true
        case Some(myInstance) =>
          val instances: Seq[Leavu3Instance] = instanceLkup.values.map(_.item).toSeq.sortBy(_.id)
          val highestPriority = instances.map(_.priority).max
          val instancesWithHighestPrio = instances.filter(_.priority == highestPriority)
          if (instancesWithHighestPrio.size == 1) {
            instancesWithHighestPrio.head.id == ownInstanceId
          } else {
            instances.head.id == ownInstanceId
          }
      }
    }
  }
}

/**
  * Created by kjolh on 3/20/2016.
  */

object DcsRemote {

  private var instance: DcsRemote = null

  def init(appCfg: Configuration): Unit = {
    require(instance == null, "Cannot call DcsRemote.init twice!")
    instance = DcsRemote(appCfg)
  }

  implicit def remote2client(r: DcsRemote.type): RestClient = instance.client
  implicit def remote2remote(r: DcsRemote.type): DcsRemote = instance

  def remoteConfig: DcsRemoteRemoteConfig = instance.remoteConfig

  case class Stored[T](timestamp: Double, age: Double, item: T)

}
