package com.example.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object MaterlizingValue extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  import system.dispatcher
  /*val source = Source(1 to 10)
  val sink = Sink.reduce[Int](_+_)
  val graph = source.runWith(sink)
  graph.onComplete{
    case Success(value) => println(s"got output value of $value")
    case Failure(ex) => println(s"got exception $ex")
  }*/
  val list = List("hello", "how are you neha", "I am fine thank you")
  val simplesource = Source(list)
  val simplesink = Sink.foreach(println)
  val flow = Flow[String].map(x => x.length)
  //val graph = simplesource.viaMat(flow)(Keep.right).toMat(simplesink)(Keep.right).run()
  val graph =
    simplesource.viaMat(flow)(Keep.left).toMat(simplesink)(Keep.right).run()
  graph.onComplete {
    case Success(value) => println(s"got output value of $value")
    case Failure(ex)    => println(s"got exception $ex")
  }

}
