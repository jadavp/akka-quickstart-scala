package com.example.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object StreamDemo1 extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val source = Source(1 to 10)
  val sink = Sink.foreach[Int](println)
  val graph = source.to(sink)
  graph.run()

  val flow = Flow[Int].map(x => x + 1)
  source.via(flow).to(sink).run()
}
