package com.example.Udemy

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.example.Udemy.TimeAssertaionSpec.{WorkActor, WorkReply}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.util.Random

class TimeAssertaionSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A sender" should {
    "receive message in timely manner" in {
      val actor = system.actorOf(Props[WorkActor])
      actor ! "hello"
      within(500.millis, 1.second) {
        expectMsg(WorkReply(42))
      }
    }
    "aggregate message in timely manner" in {
      val actor = system.actorOf(Props[WorkActor])
      actor ! "good"
      within(100.millis, 1.second) {
        val result: Seq[Int] = receiveWhile[Int](300.millisecond, 2.second, messages = 10) {
          case WorkReply(number) => number
        }
        assert(result.sum > 5)
      }

    }

  }


}

object TimeAssertaionSpec {

  case class WorkReply(value: Int)

  class WorkActor extends Actor {
    override def receive: Receive = {
      case "hello" =>
        Thread.sleep(500)
        sender() ! WorkReply(42)
      case "good" =>
        val r = new Random()
        for (_ <- 1 to 10) {
          Thread.sleep(r.nextInt(50))
          sender() ! WorkReply(1)
        }
    }
  }

}
