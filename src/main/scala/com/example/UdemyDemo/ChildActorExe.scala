package com.example.UdemyDemo

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.example.UdemyDemo.ChildActorExe.Parent.{CreteChild, TellChild}

object ChildActorExe extends App{

  object Parent {

    case class CreteChild(name:String)

    case class TellChild(message:String)

  }
  class Parent extends Actor {

    override def receive : Receive = {

      case CreteChild(name)=>
        val childRef = context.actorOf(Props[Child],name)
        context.become(withChild(childRef))
    }

    def withChild(actorRef:ActorRef): Receive ={
      case TellChild(message) => actorRef ! message
    }

  }

  class Child extends Actor{
    override  def receive :Receive ={
      case message: String => println(s"${self.path}  I got $message")
    }
  }


  val actor = ActorSystem("ParentChildActorSystem")
  val parent = actor.actorOf(Props[Parent],"ParentSystem")
  parent ! CreteChild("neha")
  parent ! TellChild("hello")



}
