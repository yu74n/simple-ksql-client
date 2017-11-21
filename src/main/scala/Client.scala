import Client.Select
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.pattern.pipe
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global

final case class KsqlQuery(ksql: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ksqlQuery = jsonFormat1(KsqlQuery)
}

object Client {
  def props: Props = Props[Client]

  final case class Select(query: String)

}

class Client extends Actor with ActorLogging with JsonSupport {

  import MediaTypes._

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val receiver : ActorRef = system.actorOf(Receiver.props, "receiver")

  override def receive: Receive = {

    case Select(query) =>
      val json = KsqlQuery(query).toJson
      val data = ByteString(json.compactPrint)
      Http().singleRequest(HttpRequest(HttpMethods.POST,
        uri = "http://localhost:8080/query",
        entity = HttpEntity(`application/json`, data)
      ))
        .pipeTo(receiver)
  }
}

object Receiver {
  def props: Props = Props[Receiver]
}

class Receiver extends Actor with ActorLogging {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  override def receive: Receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runForeach(body => log.info("Got response, body: " + body.utf8String))
    case resp@HttpResponse(code, _, _, _) =>
      log.info("Request failed, response code: " + code)
      resp.discardEntityBytes()
  }
}