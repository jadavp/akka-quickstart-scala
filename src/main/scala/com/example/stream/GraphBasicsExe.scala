package com.example.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{
  Broadcast,
  GraphDSL,
  RunnableGraph,
  Sink,
  Source,
  Zip
}
import com.example.stream.GraphBasics.input

object GraphBasicsExe extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val simpleSource = Source(1 to 1000)
  val sink1 = Sink.foreach[Int](x => println(s"sink1 $x"))
  val sink2 = Sink.foreach[Int](x => println(s"sink2 $x"))

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      {
        import GraphDSL.Implicits._

        //Step-2 add necessary components
        val broadcast = builder.add(Broadcast[Int](2)) // Fan Out

        //Step - 3 tying up component
        simpleSource ~> broadcast

        broadcast.out(0) ~> sink1
        broadcast.out(1) ~> sink2

        // Step -4 return a closed shape
        ClosedShape
      }
  })

  graph.run()
}
