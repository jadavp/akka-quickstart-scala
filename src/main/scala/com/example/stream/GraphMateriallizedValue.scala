package com.example.stream

import akka.actor.ActorSystem
import akka.stream.javadsl.RunnableGraph
import akka.stream.{ActorMaterializer, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}

import scala.util.{Failure, Success}

object GraphMateriallizedValue extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val wordSource = Source(List("Akka", "is", "awesome", "rock", "the", "jvm"))
  val printer = Sink.foreach[String](println)
  val counter = Sink.fold[Int, String](0)((count, _) => count + 1)

  val complexWordSink = Sink.fromGraph(
    GraphDSL.create(printer, counter)(
      (printerMatValue, counterMatvalue) => counterMatvalue
    ) { implicit builder => (printeShape, counterShape) =>
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[String](2))
      val lowerCaseFilter =
        builder.add(Flow[String].filter(x => x == x.toLowerCase()))
      val shortStringFilter =
        builder.add(Flow[String].filter(x => x.length < 5))

      broadcast ~> lowerCaseFilter ~> printeShape
      broadcast ~> shortStringFilter ~> counterShape

      SinkShape(broadcast.in)
    }
  )

  import system.dispatcher
  val shortStringCountFuture =
    wordSource.toMat(complexWordSink)(Keep.right).run()
  shortStringCountFuture.onComplete {
    case Success(count) => println(s"total number of count $count")
    case Failure(exception) =>
      println(s"The count of short String failed: $exception")
  }

}
