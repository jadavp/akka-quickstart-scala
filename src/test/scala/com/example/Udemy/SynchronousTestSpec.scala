package com.example.Udemy

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, TestActorRef, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._

class SynchronousTestSpec extends WordSpecLike with BeforeAndAfterAll {
  import SynchronousTestSpec._
  implicit val system =  ActorSystem("SynchronousTestSpec")
  override def afterAll():Unit ={
    system.terminate()
  }

  "A counter" should {
    "synchonously increase its counter" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter ! Inc
      assert(counter.underlyingActor.count==1)
    }

    "synchonously increase its counter iat the call of receive function" in {
      val counter = TestActorRef[Counter](Props[Counter])
      counter.receive(Inc)
      assert(counter.underlyingActor.count==1)
    }

    "work on the calling thread dispatcher" in {
      val counter = system.actorOf(Props[Counter].withDispatcher(CallingThreadDispatcher.Id))
      val testProbe = TestProbe()
      testProbe.send(counter,Read)
      testProbe.expectMsg(Duration.Zero,0)
    }
  }

}

object SynchronousTestSpec {

  case object Inc

  case object Read

  class Counter extends Actor {
    var count: Int = 0

    override def receive: Receive = {
      case Inc => count += 1
      case Read => sender() ! count
    }

  }

}