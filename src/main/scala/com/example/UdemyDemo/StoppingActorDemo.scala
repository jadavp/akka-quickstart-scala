package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}

object StoppingActorDemo extends  App {
  import Master._
  object Master {

    case class StartChild(name: String)

    case class StopChild(name: String)

    case object Stop

  }

  class Master extends Actor with ActorLogging {


    override def receive: Receive = withChildren(Map())

    def withChildren(childrenMap: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"[master] creating child $name")
        context.become(withChildren(childrenMap + (name -> context.actorOf(Props[Child], name))))
      case StopChild(name) =>
        log.info(s"[master] Stopping child $name")
        val childOption = childrenMap.get(name)
        childOption.foreach(childRef => context.stop(childRef))
      case Stop =>
        log.info(s"[master] Stopping master")
        context.stop(self)
      case message:String =>
        log.info(s"[master] ${message.toString}")
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message: String =>
        log.info(s"[${self.path}] ${message.toString}")
    }

  }

  val system = ActorSystem("StoppingActorDemo")
 /*val master = system.actorOf(Props[Master],"Master")
  master ! StartChild("child1")
  val child = system.actorSelection("/user/Master/child1")
  child ! "Hello How are you child1"
  master ! StopChild("child1")
  //for(_  <- 1 to 10){child ! "Hello How are you child1"}
  master ! Stop
  for(_  <- 1 to 10) master ! "Hello Master How are you"*/

/*  val looseactor = system.actorOf(Props[Child],"LooseActoe")
  looseactor ! "hello kid how are you?"
  looseactor ! PoisonPill
  looseactor ! "Are you still awake after PoisonPill"

  val killedActor = system.actorOf(Props[Child],"KilledActor")
  killedActor ! "hello kid how are you?"
  killedActor ! Kill
  killedActor ! "Are you still awake after Kill Command"*/

}
