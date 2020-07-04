package com.example.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object StreamExe1 extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val nameList = List("prakash", "ravi", "pooja", "NehaRani")
  val source = Source(nameList)
  val sink = Sink.foreach[String](println)
  val flow = Flow[String].filter(x => x.length > 5)
  source.via(flow).to(sink).run()
  source.filter(x => x.length > 5).runForeach(println)
}
