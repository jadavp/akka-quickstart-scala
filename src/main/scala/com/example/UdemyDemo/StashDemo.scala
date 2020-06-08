package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}

object StashDemo extends App {

  case object Open
  case object Close
  case object Read
  case class Write(data:String)

  class ResourcesActor extends Actor with ActorLogging with Stash {
    private val innerData: String = ""


    override def receive: Receive = closed

    def closed: Receive = {
      case Open =>
        log.info("Opening the source")
        unstashAll()
        context.become(open)
      case message: String =>
        log.info(s"Stashing $message because I cant handle it in the closed state")
        stash()
    }


    def open: Receive = {
      case Read =>
        log.info(s"I have read $innerData")
      case Write(data) =>
        log.info(s"I am writing data $data")
      case Close =>
        log.info("Closing resources")
        unstashAll()
        context.become(closed)
      case message =>
        log.info(s"Stashing $message because I cant handle it in open state")
        stash()
    }
  }

  val system = ActorSystem("StashDemo")
  val resources = system.actorOf(Props[ResourcesActor])

  resources ! Read
  resources ! Open
  resources ! Open
  resources ! Write("I love Akka")
  resources ! Close
  resources ! Read

}
