package se.gigurra.leavu3.util

import java.net.InetAddress

import com.twitter.finagle.Http
import com.twitter.finagle.http._
import com.twitter.util.{JavaTimer, _}
import se.gigurra.serviceutils.twitter.service.ServiceException

/**
  * Created by kjolh on 3/10/2016.
  */
case class RestClient(addr: String, port: Int, name: String)(implicit val timer: JavaTimer = DefaultTimer.underlying) {

  // Check valid address first
  try InetAddress.getByName(addr) catch {
    case NonFatal(e) => throw new RuntimeException(s"Unable to connect to $name @ $addr:$port", e)
  }

  private val client = Http.client.newService(s"$addr:$port")
  private val throttler = Throttler[String]()
  private val timeout = Duration.fromSeconds(3)

  def get(path: String, maxAge: Option[Duration] = None, minTimeDelta: Option[Duration] = None): Future[String] = {
    throttler.access(path, minTimeDelta)(doGet(path, maxAge))
  }

  def put(path: String, minTimeDelta: Option[Duration] = None)(data: => String): Future[Unit] = {
    throttler.access(path, minTimeDelta)(doPut(path, data))
  }

  def delete(path: String, minTimeDelta: Option[Duration] = None): Future[Unit] = {
    throttler.access(path, minTimeDelta)(doDelete(path + "?cache_only=true"))
  }

  def post(path: String, minTimeDelta: Option[Duration] = None)(data: => String): Future[Unit] = {
    throttler.access(path, minTimeDelta)(doPost(path, data))
  }

  private def doGet(path: String, cacheMaxAgeMillis: Option[Duration] = None): Future[String] = {
    val params = cacheMaxAgeMillis.toSeq.map(x => "max_cached_age" -> x.inMillis.toString)
    val request = Request(path, params:_*)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.value(response.contentString)
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  private def doPost(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Post, path)
    request.contentString = data
    request.contentLength = request.length
    request.contentType = "application/json"
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  private def doDelete(path: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Delete, path)
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }

  private def doPut(path: String, data: String): Future[Unit] = {
    val request = Request(Version.Http11, Method.Put, path)
    request.contentString = data
    request.contentLength = request.length
    request.contentType = "application/json"
    client.apply(request).raiseWithin(timeout).flatMap {
      case OkResponse(response)  => Future.Unit
      case BadResponse(response) => Future.exception(ServiceException(response))
    }
  }
}

object OkResponse {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.Ok  => Some(response)
    case _          => None
  }
}

object NotFound {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.NotFound  => Some(response)
    case _                => None
  }
}

object BadResponse {
  def unapply(response: Response): Option[Response] = response.status match {
    case Status.Ok  => None
    case _          => Some(response)
  }
}

case class IdenticalRequestPending(id: String) extends RuntimeException(s"Identical request to id $id already pending")
