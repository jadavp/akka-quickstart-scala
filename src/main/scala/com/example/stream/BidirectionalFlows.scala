package com.example.stream

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, BidiShape, ClosedShape}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source}

object BidirectionalFlows extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  def encrypt(n: Int)(string: String) = string.map(c => (c + n).toChar)

  def decrypt(n: Int)(string: String) = string.map(c => (c - n).toChar)

  val bidiCryptoStaticGraph = GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._
    val encryptionFlowShape = builder.add(Flow[String].map(encrypt(3)))
    val decryptionFlowShape = builder.add(Flow[String].map(decrypt(3)))
    BidiShape.fromFlows(encryptionFlowShape, decryptionFlowShape)
  }

  val unencryptedStrings =
    List("akka", "is", "awesome", "testing", "bidirection", "flow")
  val unencryptedSource = Source(unencryptedStrings)
  val encryptedSource = Source(unencryptedStrings.map(encrypt(3)))

  val cryptoBidi = RunnableGraph
    .fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val unencryptedSourceShape = builder.add(unencryptedSource)
      val encryptedSourceShape = builder.add(encryptedSource)
      val bidi = builder.add(bidiCryptoStaticGraph)
      val encryptedSink = builder
        .add(Sink.foreach[String](string => println(s"Encrypted: $string")))
      val decryptedSink = builder
        .add(Sink.foreach[String](string => println(s"Decrypted: $string")))

      unencryptedSourceShape ~> bidi.in1; bidi.out1 ~> encryptedSink
      decryptedSink <~ bidi.out2; bidi.in2 <~ encryptedSourceShape
      ClosedShape
    })
    .run()

}
