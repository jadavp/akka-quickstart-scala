package com.example.stream

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future

object IntegratingWIthhExtrenalServices extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  implicit  val dispatcher = system.dispatchers.lookup("dedicated-dispatcher")
  def genericExtService[A,B](element:A):Future[B]= ???

  case class PagerEvent(appliation:String,description:String,date:Date)

  val eventSource = Source(List(
    PagerEvent("AkkaInfra","Infra broke",new Date),
    PagerEvent("FastDataPipeline","Illegeal Infra Pipeline",new Date),
    PagerEvent("AkkaInfra","A Service stopped responding",new Date),
    PagerEvent("SuperFrontend","A button doesnt work",new Date)
  ))

  object PagerService{
    private val engineers = List("Daniel","Prakash","Neha")
    private val emails = Map( "Daniel" -> "danie@rtvm.com",
      "Prakash" -> "prakash@yahoo.com","Neha" -> "Neha@mail.com")


    def processEvent(pagerEvent: PagerEvent)= Future{
      val enineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 36000)) & engineers.length
      val engineer = engineers(enineerIndex.toInt)
      val engineerEmail = emails(engineer)

      println(s"Sending enginner $engineerEmail a high prority notification $pagerEvent")
      Thread.sleep(1000)
      engineerEmail
    }
  }

  val infraEvent = eventSource.filter((_.appliation == "AkkaInfra"))
  val PagedEngineerEmail = infraEvent.mapAsync(parallelism = 4)(event => PagerService.processEvent(event) )
  val pagedEmailSink = Sink.foreach[String](email => println(s"Sent email notification to $email"))

  PagedEngineerEmail.runWith(pagedEmailSink)

  class PagerActor extends Actor with ActorLogging{
    private val engineers = List("Daniel","Prakash","Neha")
    private val emails = Map( "Daniel" -> "danie@rtvm.com",
      "Prakash" -> "prakash@yahoo.com","Neha" -> "Neha@mail.com")


    def processEvent(pagerEvent: PagerEvent)= {
      val enineerIndex = (pagerEvent.date.toInstant.getEpochSecond / (24 * 36000)) & engineers.length
      val engineer = engineers(enineerIndex.toInt)
      val engineerEmail = emails(engineer)

      println(s"Sending enginner $engineerEmail a high prority notification $pagerEvent")
      Thread.sleep(1000)
      engineerEmail
    }

    override def receive: Receive = {
      case pagerEvent: PagerEvent =>
        sender ! processEvent(pagerEvent)
    }
  }
  import scala.concurrent.duration._
  implicit val timeout = Timeout(4.seconds)
  val pagerActor = system.actorOf(Props[PagerActor],"pagerActor")
  val altenativePagedEngineerEmails = infraEvent.mapAsync(parallelism = 4)(event =>(pagerActor ? event).mapTo[String])
  altenativePagedEngineerEmails.to(pagedEmailSink)

  //Dont confuse mapAsync with async(ASYNC boundry)
}
