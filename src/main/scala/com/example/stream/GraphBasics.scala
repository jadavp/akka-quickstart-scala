package com.example.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, Source, Zip}

object GraphBasics extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  val input = Source(1 to 1000)
  val incrementer = Flow[Int].map(x => x + 1)
  val multiplier = Flow[Int].map(x => x * 10)
  val output = Sink.foreach[(Int, Int)](println)

  //step-1 Setting Up Fundamental for GraphDSL
  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      {
        import GraphDSL.Implicits._

        //Step-2 add necessary components
        val broadcast = builder.add(Broadcast[Int](2)) // Fan Out
        val zip = builder.add(Zip[Int, Int]) // Fan In

        //Step - 3 tying up component
        input ~> broadcast

        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1

        zip.out ~> output

        // Step -4 return a closed shape
        ClosedShape
      }
  })

  graph.run()

}
