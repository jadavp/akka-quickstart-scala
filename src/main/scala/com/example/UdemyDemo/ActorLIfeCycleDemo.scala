package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object ActorLIfeCycleDemo extends App {

  case object FailChild

  case object Fail

  case object Check

  case object CheckChild


  class Parent extends Actor {
    val child = context.actorOf(Props[Child], "Child")

    override def receive: Receive = {
      case FailChild => child ! Fail
      case CheckChild => child ! Check
    }
  }

  class Child extends Actor with ActorLogging {

    override def preStart(): Unit = log.info("[Child] I am starting to process messages....")

    override def postStop(): Unit = log.info("[Child] I am stopping to process messages due to exception....")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.info(s"[Child] I failed due to $reason and restarting")

    override def postRestart(reason: Throwable): Unit = log.info(s"[Child] I have restarted successfully  due to $reason")

    override def receive: Receive = {
      case Fail =>
        log.warning("[Child] I am going to fail")
        throw new RuntimeException("I failed")
      case Check =>
        log.info("[Child]I am alive and kicking ")


    }
  }

  val system = ActorSystem("ActorLifeCycleSystem")
  val parent = system.actorOf(Props[Parent], "Parent")
  parent ! FailChild
  parent ! CheckChild

}
