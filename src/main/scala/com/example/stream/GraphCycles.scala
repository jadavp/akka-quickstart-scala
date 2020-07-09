package com.example.stream

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{
  Flow,
  GraphDSL,
  Merge,
  MergePreferred,
  RunnableGraph,
  Source
}

object GraphCycles extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val accelarator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(Merge[Int](2))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"Accelarting :$x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrementerShape
    mergeShape <~ incrementerShape
    ClosedShape
  }

  val enhancedAccelarator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(MergePreferred[Int](1))
    val incrementerShape = builder.add(Flow[Int].map { x =>
      println(s"Accelarting :$x")
      x + 1
    })

    sourceShape ~> mergeShape ~> incrementerShape
    mergeShape.preferred <~ incrementerShape
    ClosedShape
  }

  val bufferAccelarator = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val sourceShape = builder.add(Source(1 to 100))
    val mergeShape = builder.add(MergePreferred[Int](1))
    val incrementerShape =
      builder.add(Flow[Int].buffer(10, OverflowStrategy.dropHead).map { x =>
        println(s"Accelarting :$x")
        Thread.sleep(100)
        x
      })

    sourceShape ~> mergeShape ~> incrementerShape
    mergeShape.preferred <~ incrementerShape
    ClosedShape
  }
  RunnableGraph.fromGraph(bufferAccelarator).run()
  //RunnableGraph.fromGraph(accelarator).run()
  //RunnableGraph.fromGraph(enhancedAccelarator).run()
}
