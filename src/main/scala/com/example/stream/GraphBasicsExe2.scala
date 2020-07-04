package com.example.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{
  Balance,
  Broadcast,
  GraphDSL,
  Merge,
  RunnableGraph,
  Sink,
  Source
}

object GraphBasicsExe2 extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  val input = Source(1 to 1000)
  import scala.concurrent.duration._
  val fastSource = input.throttle(5, 1.second)
  val slowSource = input.throttle(2, 1.second)
  val sink1 = Sink.foreach[Int](x => println(s"sink1 $x"))
  val sink2 = Sink.foreach[Int](x => println(s"sink2 $x"))

  val graph = RunnableGraph.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      {
        import GraphDSL.Implicits._

        //Step-2 add necessary components
        val merge = builder.add(Merge[Int](2))
        val balance = builder.add(Balance[Int](2)) // Fan Out

        //Step - 3 tying up component
        slowSource ~> merge ~> balance ~> sink1
        fastSource ~> merge; balance ~> sink2

        // Step -4 return a closed shape
        ClosedShape
      }
  })
  graph.run()
}
