package com.example.stream

import java.util.Date

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}


object AdvanceBackpressure extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  //control backPressure
  val controlFlow = Flow[Int].map(_ * 2).buffer(10, OverflowStrategy.dropHead)

  case class PagerEvent(description: String, date: Date, nInstances:Int = 1)

  case class Notification(email: String, pagerEvent: PagerEvent)

  val events = List(
    PagerEvent("ServiceDeiscoveryFailed",new Date),
    PagerEvent("Illegal elements",new Date),
    PagerEvent("Number of HTTP500 spiked",new Date),
    PagerEvent("A services stopped responding",new Date)
  )

  val eventSource = Source(events)
  val oncallEngineer = "daniel@com.com"
  def sendEmail(notification: Notification) =
    println(s"Dear ${notification.email} you have an event : ${notification.pagerEvent}")

  val notificatiSink = Flow[PagerEvent].map(event => Notification(oncallEngineer,event))
    .to(Sink.foreach[Notification](sendEmail))

  /*eventSource.runWith(notificatiSink)*/

  def sendEmailSlow(notification: Notification) = {
    Thread.sleep(1000)
    println(s"Dear ${notification.email} you have an event : ${notification.pagerEvent}")
  }


  val aggregateNotificationFlow = Flow[PagerEvent].conflate((event1,event2) => {
      val nIntances = event1.nInstances + event2.nInstances
      PagerEvent(s"You ahve $nIntances events that require your attention", new Date,nIntances)
    }).map(event => Notification(oncallEngineer,event))


  //eventSource.via(aggregateNotificationFlow).async.to(Sink.foreach[Notification](sendEmailSlow)).run()
  //alternate to backpressure

  // Slow producers: extrapolate/expand
  import scala.concurrent.duration._
  val slowcounter = Source(Stream.from(1)).throttle(1, 1.second)
  val hungrySink = Sink.foreach[Int](println)
  val extrapolator = Flow[Int].extrapolate(element => Iterator.from(element))
  val repeater = Flow[Int].extrapolate(element => Iterator.continually(element))
  val enpander = Flow[Int].expand(element => Iterator.continually(element))
  slowcounter.via(extrapolator).to(hungrySink).run()

}
