package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.Logging

object ActorLoggingDemo extends App{

  class SimpleActorWIthExplicitLogging extends Actor {
    val logger = Logging(context.system,this)
    override def receive:Receive ={
      case message:String => logger.info(message)
    }
  }

  class SimpleActorWIthActorLogging extends Actor with ActorLogging {
    override def receive:Receive ={
      case message:String => log.info(message)
    }
  }

  val actorSystem = ActorSystem("VotingSystem")
  val prakash = actorSystem.actorOf(Props[SimpleActorWIthExplicitLogging], "Prakash")
  val neha = actorSystem.actorOf(Props[SimpleActorWIthActorLogging], "Neha")
  prakash ! "Hello How are you?"
  neha ! "Hello How are you?"
}
