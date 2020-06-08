package com.example.Udemy

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.example.Udemy.TestProbeSpec.{Master, Register, RegisterAck, Reply, SlaveWork, Work, WorkCompleted}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class TestProbeSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit ={
    TestKit.shutdownActorSystem(system)
  }



  "A master node should" should {
    "send receive ack to sender" in {
      val master = system.actorOf(Props[Master])
      val slave = TestProbe("slave")
      master ! Register(slave.ref)
      slave.expectMsg(RegisterAck)
      val workLoadString = "Hello neha, I love you"
      master ! Work(workLoadString)
      slave.expectMsg(SlaveWork(workLoadString, testActor))
      slave.reply(WorkCompleted(3, testActor))
      expectMsg(Reply(3))
    }

      "aggeregate values  t sender" in {
        val master = system.actorOf(Props[Master])
        val slave = TestProbe("slave")
        master ! Register(slave.ref)
        slave.expectMsg(RegisterAck)
        val workLoadString = "Hello neha, I love you"
        master ! Work(workLoadString)
        master ! Work(workLoadString)

        slave.receiveWhile(){
          case SlaveWork(`workLoadString`,`testActor`) => slave.reply(WorkCompleted(3,testActor))
        }

        expectMsg(Reply(3))
        expectMsg(Reply(6))
      }
  }


}


object TestProbeSpec {

  case class Register(slaveRef: ActorRef)

  case class Work(text: String)

  case class SlaveWork(str: String, ref: ActorRef)

  case class WorkCompleted(count: Int, originalSender: ActorRef)

  case class Reply(count: Int)

  case object RegisterAck

  class Master extends Actor {
    override def receive: Receive = {
      case Register(slaveRef) =>
        slaveRef ! RegisterAck
        context.become(online(slaveRef, 0))
      case _ =>
    }

    def online(slaveRef: ActorRef, totalWordCount: Int): Receive = {
      case Work(text) => slaveRef ! SlaveWork(text, sender())
      case WorkCompleted(count, originalSender) =>
        val newTotalCount = totalWordCount + count
        originalSender ! Reply(newTotalCount)
        context.become(online(slaveRef, newTotalCount))
    }
  }


}