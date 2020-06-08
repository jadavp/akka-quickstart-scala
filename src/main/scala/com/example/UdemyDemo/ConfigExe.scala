package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object ConfigExe extends App{

  class SimpleActor extends Actor with ActorLogging{

    override def receive: Receive ={
      case message:String => log.info(s"Hello $message")
    }

  }

  val actorSystem = ActorSystem("configActorSystem")
  val defaultActorSystem = actorSystem.actorOf(Props[SimpleActor])
  defaultActorSystem ! "AKKA is good and Scala is very Good"

  val specialConfig = ConfigFactory.load().getConfig("simpleconfig")
  val specialActorSystem  = ActorSystem("SpecialActorSyste",specialConfig)
  val specialActor = specialActorSystem.actorOf(Props[SimpleActor])
  specialActor ! "JAVA is not good"

  val separateConfig = ConfigFactory.load("secrentConfig/separateconfig.conf")
  println(s"config: ${separateConfig.getString("akka.loglevel")}")

  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"config: ${separateConfig.getString("akka.loglevel")}")

  val propsConfig = ConfigFactory.load("props/propsConfig.properties")
  println(s"config: ${separateConfig.getString("akka.loglevel")}")
}
