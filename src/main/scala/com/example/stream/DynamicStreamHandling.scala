package com.example.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches}

import scala.concurrent.duration._

object DynamicStreamHandling extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val killSwitchSource = KillSwitches.single[Int]
  val counter = Source(Stream.from(1)).throttle(1, 1.second)
  val sink = Sink.foreach[Int](println)

  // val killmaterilizer = counter.viaMat(killSwitchSource)(Keep.right).to(sink).run()

  /* system.scheduler.scheduleOnce(3.second){
     killmaterilizer.shutdown()
   }*/
  val counter1 = Source(9000 to 90000).throttle(1, 1.second)
  val masterKill = KillSwitches.shared("MasterKill")

  /*  counter.via(masterKill.flow).to(sink).run()
    counter1.via(masterKill.flow).to(sink).run()*/

  /*system.scheduler.scheduleOnce(6.second){
    masterKill.shutdown()
    system.terminate()
  }*/

  val dynamicmerge = MergeHub.source[Int]
  val materlizedSource = dynamicmerge.to(Sink.foreach(e => println(s"I hve received this in a mergesink :$e"))).run()
  //counter.runWith(materlizedSource)
  //counter1.runWith(materlizedSource)

  val dynamicBroadcast = BroadcastHub.sink[Int]
  //val materlizedSink = counter.toMat(dynamicBroadcast)(Keep.right).run()

  val (publisherport, subscriberport) = dynamicmerge.toMat(dynamicBroadcast)(Keep.both).run()

  val counter2 = Source(1 to 10)
  val counter3 = Source(11 to 20)
  val counter4 = Source(21 to 30)

  val sink2 = Sink.foreach[Int](ele => println(s"Sink2 : $ele"))
  val sink3 = Sink.foreach[Int](ele => println(s"Sink3 : $ele"))

  counter2.to(publisherport).run()
  counter3.to(publisherport).run()
  counter4.to(publisherport).run()
  subscriberport.to(sink2).run()



}
