package com.example.ActorsWithStream

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout

import scala.concurrent.duration._

object IntegratingWithActors extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  class SimpleActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case s: String =>
        log.info(s"just received String $s")
        sender() ! s"$s$s"
      case n: Int =>
        log.info(s"just received Integer $n")
        sender() ! (2 * n)
      case _ =>
    }
  }

  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  val numberSource = Source(1 to 10)
  implicit val timeout = Timeout(2.second)
  val actorBasedFlow = Flow[Int].ask[Int](parallelism = 4)(simpleActor)

  //numberSource.via(actorBasedFlow).to(Sink.ignore).run()
  // numberSource.ask[Int](parallelism = 4)(simpleActor).to(Sink.ignore).run()

  val actorPowerSource = Source.actorRef[Int](
    bufferSize = 10,
    overflowStrategy = OverflowStrategy.dropHead
  )
  val materlizedActorRef =
    actorPowerSource.to(Sink.foreach[Int](n => println(s"number is $n"))).run()
  // materlizedActorRef ! 10
  //materlizedActorRef ! akka.actor.Status.Success("complete")

  case object StreamInit
  case object StreamAck
  case object SteamComplete
  case class StremFail(ex: Throwable)

  class DestinationActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case StreamInit =>
        log.info(s"Stream Intialized")
        sender() ! StreamAck
      case SteamComplete =>
        log.info("stream complete")
        context.stop(self)
      case StremFail(ex) =>
        log.warning("Stream failed : $ex")
      case message =>
        log.info(s"Message $message has come to its resting place")
        sender() ! StreamAck
    }
  }

  val destinationActor =
    system.actorOf(Props[DestinationActor], "destinationActor")
  val actorPoweredSink = Sink.actorRefWithBackpressure[Int](
    destinationActor,
    onInitMessage = StreamInit,
    onCompleteMessage = SteamComplete,
    ackMessage = StreamAck,
    onFailureMessage = throwable => StremFail(throwable)
  )

  Source(1 to 10).to(actorPoweredSink).run()
}
