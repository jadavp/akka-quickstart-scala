package com.example.UdemyDemo

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.example.UdemyDemo.ChildActorExe2.WordCounterMater.{Initialize, WordCountTask, WordCountrTaskReply}

object ChildActorExe2 extends App {

  //Disctributed Word Account

  object WordCounterMater {

    case class Initialize(node: Int)

    case class WordCountTask(id: Int, text: String)

    case class WordCountrTaskReply(id: Int, numerOfWord: Int)

  }

  class WordCounterMater extends Actor {
    override def receive: Receive = {
      case Initialize(n) =>
        println("[master] Intilizing child nodes...")
        val childernRefs: Seq[ActorRef] = for (i <- 1 to n) yield context.actorOf(Props[WordCounterWorker], s"Child$i")
        context.become(withChildren(childernRefs, 0, 0, Map()))
    }

    def withChildren(childernRefs: Seq[ActorRef], currentChildIndex: Int, currentTaskID: Int, requestMap: Map[Int, ActorRef]): Receive = {
      case message: String =>
        println(s"[master]I have received $message and send it to $currentChildIndex for processing ")
        val originalSender = sender()
        val task = WordCountTask(currentTaskID, message)
        val childref = childernRefs(currentChildIndex)
        childref ! task
        val nextChildIndex = (currentChildIndex + 1) % childernRefs.length
        val newTaskId = currentTaskID + 1
        val newRequestMap = requestMap + (currentTaskID -> originalSender)
        context.become(withChildren(childernRefs, nextChildIndex, newTaskId, newRequestMap))
      case WordCountrTaskReply(id, count) =>
        println(s"[master I have received a reply with task id $id with $count]")
        val originalsender = requestMap(id)
        originalsender ! count
        context.become(withChildren(childernRefs, currentChildIndex, currentTaskID, requestMap - id))


    }
  }

  class WordCounterWorker extends Actor {
    override def receive: Receive = {
      case WordCountTask(id, message) =>
        println(s"[child${self.path}] I have received task $id with $message")
        sender() ! WordCountrTaskReply(id, message.length)
    }
  }
  class TestActor extends Actor {
    override def receive :Receive ={
      case "go" =>
        val master = context.actorOf(Props[WordCounterMater],"master")
        master ! Initialize(3)
        val texts = List("hello akkka","akka is good","scala is not bad","java is bad")
        texts.foreach(text => master ! text)
      case count: Int =>
        println(s"[test actor] I received a reply: $count")
    }
  }

  val actorSystem = ActorSystem("ParentChildActorSystem")
  val testActor = actorSystem.actorOf(Props[TestActor], "TestActor")
  testActor ! "go"

}
