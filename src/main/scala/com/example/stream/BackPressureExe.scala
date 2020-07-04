package com.example.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.util.{Failure, Success}

object BackPressureExe extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  val fastSource = Source(1 to 1000)
  val slowSink = Sink.foreach[Int] { x =>
    //Thread.sleep(1000);
    println(x)
  }

  val graph = fastSource.runWith(slowSink)
  import system.dispatcher
  graph.onComplete {
    case Success(_)  => system.terminate()
    case Failure(ex) => println(ex)
  }
}
