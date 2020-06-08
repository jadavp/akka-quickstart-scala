package com.example.UdemyDemo

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorSystem, OneForOneStrategy, Props}
import akka.pattern.{Backoff, BackoffSupervisor}
import scala.concurrent.duration._
import scala.io.Source

object BackOffSupervisorDemo extends App {

  case object ReadFile
  class FileBasedSupervisorActor extends Actor with ActorLogging{
    var dataSource:Source = null

    override def preStart(): Unit = log.info("P" +
      "ersistance Actor is booting up")

    override def postStop(): Unit = log.warning("Persitance actor is dead")

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = log.warning(s"Persitance actor is restarting after $reason")

    override def receive:Receive ={
      case ReadFile =>
        if(dataSource == null)
          dataSource = Source.fromResource("testFiles/important_fail.txt")
        log.info("I have read files and convert it to list"+dataSource.getLines().toList)
    }
  }
  val system = ActorSystem("SimplectorSystem")
  val simpleActor = system.actorOf(Props[FileBasedSupervisorActor],"SimpleActor")
  //simpleActor ! ReadFile

  val simpleSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      Props[FileBasedSupervisorActor],
      "SimpleBackOffActor",
      3.seconds,
      30.seconds,
      0.2
    )
  ) //Child Actor

  //val simpleBackoffSupervisor = system.actorOf(simpleSupervisorProps,"SimpleSupervisor") //Parent Actor
  //simpleBackoffSupervisor ! ReadFile

  val stopSupervisorProps = BackoffSupervisor.props(
    Backoff.onFailure(
      Props[FileBasedSupervisorActor],
      "StopBackOffActor",
      3.seconds,
      30.seconds,
      0.2
    ).withSupervisorStrategy(
      OneForOneStrategy(){
        case _ => Stop
      }
    )
  ) //Child Actor

  val stopBackoffSupervisor = system.actorOf(simpleSupervisorProps,"StopSupervisor") //Parent Actor
  stopBackoffSupervisor ! ReadFile
}
