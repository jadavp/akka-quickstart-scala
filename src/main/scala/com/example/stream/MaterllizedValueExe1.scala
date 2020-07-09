package com.example.stream

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object MaterllizedValueExe1 extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  def enhanceFlow[A, B](flow: Flow[A, B, _]): Flow[A, B, Future[Int]] = {
    val counterSink = Sink.fold[Int, B](0)((count, _) => count + 1)
    Flow.fromGraph(GraphDSL.create(counterSink) {
      implicit builder => counterSinkShape =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[B](2))
        val originlFlowShape = builder.add(flow)
        originlFlowShape ~> broadcast ~> counterSinkShape
        FlowShape(originlFlowShape.in, broadcast.out(1))
    })
  }

  val simpleSource = Source(1 to 42)
  val simpleFlow = Flow[Int].map(x => x)
  val simpleSink = Sink.ignore

  val enhancedFlowCounterFuture = simpleSource
    .viaMat(enhanceFlow(simpleFlow))(Keep.right)
    .toMat(simpleSink)(Keep.left)
    .run()
  import system.dispatcher
  enhancedFlowCounterFuture.onComplete {
    case Success(count) =>
      println(s"$count element went through the enhacned flow")
    case Failure(exception) =>
      println(
        s"$exception occured while counting element which went through the enhacned flow"
      )
  }

}
