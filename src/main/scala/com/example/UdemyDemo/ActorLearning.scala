package com.example.UdemyDemo

import akka.actor.{Actor, ActorSystem, Props}

object ActorLearning extends App {

  //part-1 actor system creation
  val actorsystem = ActorSystem("PrakashSystem")
  println(actorsystem.name)

  //part-2 actor creation

  class WordCounter(data:String) extends Actor {

    override def receive: Receive = {
      case message:String => println(s"hello $message $data")
      case _ => println("I cant understand message")
    }
  }

  val wordCounter = actorsystem.actorOf(Props(new WordCounter("bob")),"WordCounter")
  wordCounter ! "lara"





}
