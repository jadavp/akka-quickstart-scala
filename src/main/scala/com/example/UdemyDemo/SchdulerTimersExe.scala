package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import scala.concurrent.duration._
object SchdulerTimersExe extends App {


  class SimpleActor extends Actor with ActorLogging {

    override def receive: Receive = {
      case message =>
        log.info(message.toString)
    }
  }



  val system = ActorSystem("TimerAndSchdularSystem")
  val actor = system.actorOf(Props[SimpleActor],"SimpleActor")

  system.log.info("Starting sending message to actor on schduled time")
 import system.dispatcher //3rd Way to to provide Execution Context
 //implicit val exeutionContext = system.dispatcher //2 nd way to provide Execution Context
  system.scheduler.scheduleOnce(3.seconds){
    actor ! "[Actor] reminder"
  }//(system.dispatcher) 1 at way to provide Execution context

  val routine: Cancellable = system.scheduler.schedule(1.seconds,3.seconds){
    actor ! "[Actor] Heart Beat"
  }
  system.scheduler.scheduleOnce(5.seconds){routine.cancel()}
}
