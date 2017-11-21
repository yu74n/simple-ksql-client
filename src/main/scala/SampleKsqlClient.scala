import Client.Select
import akka.actor.{ActorRef, ActorSystem}

object SampleKsqlClient extends App {
  val system: ActorSystem = ActorSystem("sampleKsqlClient")
  val client : ActorRef = system.actorOf(Client.props, "client")

  if (args.length != 1) {
    println("simple-ksql-client <ksql query>")
  } else {
    client ! Select(args(0))
  }
}

