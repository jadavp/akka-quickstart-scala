package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props, Timers}
import scala.concurrent.duration._

object SchdularTimersDemo extends App {

  case object HeartBeat

  class SimpleActor extends Actor with ActorLogging with Timers{
    import system.dispatcher
   // var schedule = creteTimewindow()
    def creteTimewindow(): Cancellable = {
      context.system.scheduler.scheduleOnce(2.seconds){
        self ! "Timeout"
      }
    }
    case object Start
    case object Stop
    case object TimerWatch
    override  def receive:Receive ={
      case HeartBeat => log.info("I have received Heartbeat")
        timers.cancel(TimerWatch)
        //schedule.cancel()
        //schedule = creteTimewindow()
      case "Timeout" => log.info("I have received Timeout")
        timers.startSingleTimer(TimerWatch,Start,2.seconds)
        context.stop(self)

    }

  }

  val system = ActorSystem("TimerExe")
  val simpleActor = system.actorOf(Props[SimpleActor],"SimpleActor")
  import system.dispatcher
  val routine = system.scheduler.schedule(1.seconds , 1.seconds){
    simpleActor ! HeartBeat
  }

}
