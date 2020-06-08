package com.example.Udemy

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.example.Udemy.BasicSpec.{BlackHole, SimpleActor}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class BasicSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit ={
    TestKit.shutdownActorSystem(system)
  }


  "A BlackHole actor" should {
    "send back the same message" in {
      val blackHole = system.actorOf(Props[BlackHole])
      val message = "hello, Test"
      blackHole ! message
      expectNoMessage(1.second)
    }
  }

  "A simple actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, Test"
      echoActor ! message
      expectMsgPF(){
        case message:String =>

      }
    }
  }

  "A smart actor" should {
    "send back the same message" in {
      val echoActor = system.actorOf(Props[SimpleActor])
      val message = "hello, Test"
      echoActor ! message
      val reply = expectMsgType[String]
      assert(reply == message)
    }
  }

}

object BasicSpec {

  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => sender ! message
    }
  }

    class BlackHole extends Actor{
      override def receive: Receive = Actor.emptyBehavior
  }
}
