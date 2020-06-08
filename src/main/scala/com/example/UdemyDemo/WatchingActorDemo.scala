package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Terminated}
import com.example.UdemyDemo.WatchingActorDemo.Watcher.StartChild

object WatchingActorDemo extends App {

  object Watcher {

    case class StartChild(name: String)

  }

  class Watcher extends Actor with ActorLogging {

    import Watcher._

    override def receive: Receive = withChildren(Map())

    def withChildren(childrenMap: Map[String, ActorRef]): Receive = {
      case StartChild(name) =>
        log.info(s"[master] creating child $name")
        val child = context.actorOf(Props[Child], name)
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"[master] ${ref.path} Stopped")
      case message: String =>
        log.info(s"[master] ${message.toString}")
    }
  }

  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message: String =>
        log.info(s"[${self.path}] ${message.toString}")
    }
  }

  val system = ActorSystem("WatcherSystem")
  val watcher = system.actorOf(Props[Watcher],"Watcher")
  watcher ! StartChild("WatchedChiled")
  val watchedChild = system.actorSelection("/user/Watcher/WatchedChiled")
  watchedChild ! "Hello Child, You are being watched"
  watchedChild ! PoisonPill

}
