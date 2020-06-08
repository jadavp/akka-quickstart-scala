package com.example.UdemyDemo

import akka.actor.{Actor, ActorSystem, Props}
import com.example.UdemyDemo.ChangingActorBehaviour.Counter.{Print, incrementer}

object ChangingActorBehaviour extends App {

  object Counter {

    case object incrementer

    case object decremented

    case object Print

  }

  class Counter extends Actor {
    var value = 0

    override def receive: Receive = incrementerreceiver(0)

    def incrementerreceiver(i: Int): Receive = {
      case increment =>
        context.become(incrementerreceiver(i + 1))
        println(s"[updated] $i")
      case decremented =>
        context.become(incrementerreceiver(i - 1))
      case print =>
        println(s"[new Actor] $i")
    }
  }


  val actorSyestem = ActorSystem("sampleActor")
  val counter = actorSyestem.actorOf(Props[Counter], "Counter")

  counter ! incrementer
  counter ! incrementer
  counter ! Print
  counter ! incrementer

}
