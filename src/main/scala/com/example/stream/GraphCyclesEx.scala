package com.example.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{
  Broadcast,
  Flow,
  GraphDSL,
  MergePreferred,
  RunnableGraph,
  Sink,
  Source,
  Zip
}
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}

object GraphCyclesEx extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  val fibonacciGenerator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val zipShape = builder.add(Zip[BigInt, BigInt])
    val mergePreferred = builder.add(MergePreferred[(BigInt, BigInt)](1))
    val fiboLogic =
      builder.add(Flow[(BigInt, BigInt)].map(a => (a._1 + a._2, a._1)))
    val broadcast = builder.add(Broadcast[(BigInt, BigInt)](2))
    val extractLast = builder.add(Flow[(BigInt, BigInt)].map(_._1))

    zipShape.out ~> mergePreferred ~> fiboLogic ~> broadcast ~> extractLast
    broadcast ~> mergePreferred.preferred

    UniformFanInShape(extractLast.out, zipShape.in0, zipShape.in1)
  }

  val fiboGraph = RunnableGraph
    .fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val source1Shape = builder.add(Source.single[BigInt](1))
      val source2Shape = builder.add(Source.single[BigInt](1))
      val sinkShape = builder.add(Sink.foreach[BigInt](println))
      val fibo = builder.add(fibonacciGenerator)

      source1Shape ~> fibo.in(0)
      source2Shape ~> fibo.in(1)
      fibo.out ~> sinkShape

      ClosedShape
    })
    .run()
}
