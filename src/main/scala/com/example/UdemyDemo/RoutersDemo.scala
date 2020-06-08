package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing._
import com.typesafe.config.ConfigFactory

object RoutersDemo extends App {

  //Manual Router
  class Master extends Actor {
    private val slaves = for(i <- 1 to 5) yield {
      val slave = context.actorOf(Props[Child],s"Child_$i")
      context.watch(slave)
      ActorRefRoutee(slave)
    }
    private val router = Router(RoundRobinRoutingLogic(),slaves)
    override def receive : Receive ={
      case message:String => router.route(message,sender())
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newslave = context.actorOf(Props[Child])
        context.watch(newslave)
        router.addRoutee(newslave)
    }
  }



  class Child extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info((message.toString))
    }

  }

  val system = ActorSystem("RoutersDemo",ConfigFactory.load().getConfig("routersdemo"))
  val master = system.actorOf(Props[Master],"Master")
  /*for(i <- 1 to 10) {
    master  ! s"[child$i]Hello from outer World"
  }*/


  // 2.1 method to create a Router Logic in code
  val  poolMaster = system.actorOf(RoundRobinPool(5).props(Props[Child]),"SimplePoolUser")
 /* for(i <- 1 to 10) {
    poolMaster  ! s"[child$i]Hello from outer World"
  }*/

  val  poolMaster2 = system.actorOf(FromConfig.props(Props[Child]),"poolMaster2")
 /* for(i <- 1 to 10) {
    poolMaster2  ! s"[child$i]Hello from outer World"
  }*/

 //method 3.1
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Child],s"slave_$i")).toList
  val slavepaths = slaveList.map(slaveRef => slaveRef.path.toString)
  val groupMaster = system.actorOf(RoundRobinGroup(slavepaths).props())
  /*for(i <- 1 to 10) {
    groupMaster  ! s"[child$i]Hello from outer World"
  }*/
  //Method 3.2


  val groupMaster2 = system.actorOf(FromConfig.props(),"groupMaster2")
  for(i <- 1 to 10) {
    groupMaster2  ! s"[child$i]Hello from outer World"
  }

  groupMaster2 ! Broadcast("Hello, Everyone")

}
