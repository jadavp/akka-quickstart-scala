package com.example.Udemy

import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorRef, ActorSystem, AllForOneStrategy, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import akka.testkit.{ImplicitSender, TestKit}
import com.example.Udemy.SuperVisionSpec.{AllForOneSuperVisor, FussyWordCounter, NoDeathSupervisor, Report, Supervisor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class SuperVisionSpec extends TestKit(ActorSystem("SuperVisionSpec"))
  with ImplicitSender with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A supervisor" should {
    "resume its child in case of minor fault " in {
      val superVisor = system.actorOf(Props[Supervisor], "Supervisor1")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! "Hello akka how are you today"
      child ! Report
      expectMsg(3)
    }
    "restart its child in case of minor fault " in {
      val superVisor = system.actorOf(Props[Supervisor], "Supervisor2")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)
    }

    "stop its child in case of fault " in {
      val superVisor = system.actorOf(Props[Supervisor], "Supervisor3")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      watch(child)
      child ! "akka is nice"
      val terminatedMessge = expectMsgType[Terminated]
      assert(terminatedMessge.actor == child)
    }

    "escalate its child in case of fault " in {
      val superVisor = system.actorOf(Props[Supervisor], "Supervisor4")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      watch(child)
      child ! 12
      val terminatedMessge = expectMsgType[Terminated]
      assert(terminatedMessge.actor == child)
    }
  }
  "A kinder supervisor" should {
    " not kill its child in case of fault " in {
      val superVisor = system.actorOf(Props[NoDeathSupervisor], "Supervisor5")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]
      child ! "I love akka"
      child ! Report
      expectMsg(3)

      child ! 12
      child ! Report
      expectMsg(0)

    }
  }

  "All for one supervisor" should {
    " restart all child in one go " in {
      val superVisor = system.actorOf(Props[AllForOneSuperVisor], "Supervisor5")
      superVisor ! Props[FussyWordCounter]
      val child = expectMsgType[ActorRef]


      superVisor ! Props[FussyWordCounter]
      val secondchild = expectMsgType[ActorRef]


      secondchild ! "I love akka"
      secondchild ! Report
      expectMsg(3)

      child ! ""
      child ! Report
      expectMsg(0)

      Thread.sleep(1000)
      secondchild ! Report
      expectMsg(0)
    }
  }

}

object SuperVisionSpec {

  case object Report

  class Supervisor extends Actor {


    override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }

    override def receive: Receive = {
      case props: Props =>
        val childref = context.actorOf(props)
        sender() ! childref
    }
  }

  class NoDeathSupervisor extends Supervisor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {

    }
  }

  class AllForOneSuperVisor extends Supervisor {
    override val supervisorStrategy: SupervisorStrategy = AllForOneStrategy() {
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: RuntimeException => Resume
      case _: Exception => Escalate
    }
  }


  class FussyWordCounter extends Actor {
    var words = 0

    override def receive: Receive = {
      case Report => sender() ! words
      case "" => throw new NullPointerException("sentence is empty")
      case sentence: String =>
        if (sentence.length < 5) throw new RuntimeException("sendtence is too big")
        else if (!Character.isUpperCase(sentence(0))) throw new IllegalArgumentException("Illegal Argument used")
        else words += sentence.split(" ").length
      case _ => throw new Exception("can only receiver string")
    }

  }

}