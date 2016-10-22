package se.gigurra.leavu3.datamodel

import com.github.gigurra.heisenberg.MapData._
import com.github.gigurra.heisenberg.Schema
import se.gigurra.leavu3.util.CurTime
import com.github.gigurra.serviceutils.twitter.logging.Logging
import scala.language.implicitConversions

case class GameDataWire(source: SourceData = Map.empty) extends SafeParsed[GameDataWire.type] {

  // DCS Remote Metadata
  val err       = parse(schema.err)
  val requestId = parse(schema.requestId)

  // Actual game data
  val electronicWarf  = parse(schema.electronicWarf)
  val payload         = parse(schema.payload)
  val flightModel     = parse(schema.flightModel)
  val sensors         = parse(schema.sensors)
  val aiWingmenTgts   = parse(schema.aiWingmenTgts)
  val indicators      = parse(schema.indicators)
  val aiWingmen       = parse(schema.aiWingmen).collect {
    case Some(w: AiWingman) => w
  }
  val route           = parse(schema.route)
  val metaData        = parse(schema.metadata)

  def toGameData: GameData = GameData(
    err,
    requestId,
    electronicWarf,
    payload,
    flightModel,
    sensors.toSensors,
    aiWingmenTgts,
    indicators,
    aiWingmen,
    route.toRoute,
    metaData
  )

}

case class GameData(err: Option[String] = None,
                    requestId: Option[String] = None,
                    electronicWarf: ElectronicWarfare = ElectronicWarfare(),
                    payload: Payload = Payload(),
                    flightModel: FlightModel = FlightModel(),
                    sensors: Sensors = Sensors(),
                    aiWingmenTgts: Seq[Vec3] = Nil,
                    indicators: Indicators = Indicators(),
                    aiWingmen: Seq[AiWingman] = Nil,
                    route: Route = Route(),
                    metaData: MetaData = MetaData()) {

  def selfData: SelfData = metaData.selfData

  def tdcPosition: Option[Vec2] = sensors.status.tdcPosition(selfData.heading, selfData.position)
  def tdcBra: Option[Bra] = sensors.status.tdcBra(selfData.heading)
  def pdt: Option[Target] = sensors.pdt

  def aircraftMode: AircraftMode = indicators.nav.mode

  def withRoute(newRoute: Route) = copy(route = newRoute)
  def withRwsMemory(rwsContacts: Seq[Contact]): GameData = copy(sensors = sensors.withRwsMemory(rwsContacts))
  def withoutHiddenContacts: GameData = copy(sensors = sensors.withoutHiddenContacts)

  val timeStamp: Double = CurTime.seconds
  def age: Double = CurTime.seconds - timeStamp

  def hasRequestId: Boolean = requestId.isDefined
  def timedOut: Boolean = age > 3.0
  def hasError: Boolean = err.isDefined

  def isValid: Boolean = hasRequestId && !hasError && !timedOut
}

object GameDataWire extends Schema[GameDataWire] with Logging {

  // DCS Remote Metadata
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")

  // Actual game data
  val electronicWarf  = required[ElectronicWarfare]("electronicWarfare", default = ElectronicWarfare())
  val payload         = required[Payload]("payload", default = Payload())
  val flightModel     = required[FlightModel]("flightModel", default = FlightModel())
  val sensors         = required[SensorsWire]("sensor", default = SensorsWire())
  val aiWingmenTgts   = required[Seq[Vec3]]("wingTargets", default = Seq.empty)
  val indicators      = required[Indicators]("indicators", default = Indicators())
  val aiWingmen       = required[Seq[Option[AiWingman]]]("wingMen", default = Seq.empty)
  val route           = required[RouteWire]("route", default = RouteWire())
  val metadata        = required[MetaData]("metaData", default = MetaData())
}

case class ExportVersion(source: SourceData = Map.empty) extends SafeParsed[ExportVersion.type] {
  val err       = parse(schema.err)
  val requestId = parse(schema.requestId)
  val version   = parse(schema.version)
}

object ExportVersion extends Schema[ExportVersion] with Logging {
  val err       = optional[String]("err")
  val requestId = optional[String]("requestId")
  val version   = required[Int]("version", default = -1)
}

