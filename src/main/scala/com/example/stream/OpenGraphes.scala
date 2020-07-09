package com.example.stream

import akka.actor.ActorSystem
import akka.stream.impl.Compose
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape, SinkShape, SourceShape}

object OpenGraphes extends App {
  implicit val system = ActorSystem("OpenGraphes")
  implicit val materializer = ActorMaterializer

  val firstsource = Source(1 to 10)
  val secondsource = Source(42 to 1000)

  val sourceGraph =
    Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val concat = builder.add(Concat[Int](2))

      firstsource ~> concat
      secondsource ~> concat
      SourceShape(concat.out)
    })

  // sourceGraph.to(Sink.foreach(println)).run()
//Complex Sink

  val sink1 = Sink.foreach[Int](x => println(s"Meaningful $x"))
  val sink2 = Sink.foreach[Int](x => println(s"Meaningful $x"))

  val sinkGraph =
    Sink.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val broadcast = builder.add(Broadcast[Int](2))
      broadcast ~> sink1
      broadcast ~> sink2
      SinkShape(broadcast.in)
    })

  // sourceGraph.to(sinkGraph).run()

  val incrementer = Flow[Int].map(_ + 1)
  val mulitplier = Flow[Int].map(_ * 10)

  val flowGraph =
    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val incrementeShape = builder.add(incrementer)
      val multiplierShape = builder.add(mulitplier)
      incrementeShape ~> multiplierShape
      FlowShape(incrementeShape.in, multiplierShape.out)
    })

  secondsource
    .via(flowGraph)
    .to(Sink.foreach[Int](x => println(s"value id $x")))
    .run()

}
