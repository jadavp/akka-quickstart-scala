package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object  DispatchersDEmo extends App {

   class Counter extends Actor with ActorLogging{
     var count = 0
     override def receive :Receive ={
       case message =>
         count += 1
         log.info(s"[$count] $message")
     }
   }
  // method 1
  val system = ActorSystem("DispaccherDemo")
  val actors = for (i <- 1 to 10 ) yield system.actorOf(Props[Counter].withDispatcher("my-dispatcher"),s"counter_$i")
  val r = new Random()
  /*for (i <- 1 to 1000){
    actors(r.nextInt(10)) ! i
  }*/

  //method 2
  val rtjvmAtor = system.actorOf(Props[Counter],"rtjvm")

  class DBActor extends Actor with ActorLogging {
    implicit  val executionContext:ExecutionContext =context.system.dispatchers.lookup("my-dispatcher")
    override def receive:Receive = {
      case message:String => Future{
        Thread.sleep(500)
        log.info(s"Sucess : $message")}
      }

    val dbActor = system.actorOf(Props[DBActor])
    dbActor ! "The meaning of life is 42"

  }

}
