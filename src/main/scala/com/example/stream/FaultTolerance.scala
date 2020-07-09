package com.example.stream

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy.Restart
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.javadsl.RestartSource
import akka.stream.scaladsl.{Sink, Source}

import scala.util.Random

object FaultTolerance extends  App{

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val faultySource = Source(1 to 10).map(e => if(e == 6) throw new RuntimeException else e)
  //faultySource.log("trackingElements").runWith(Sink.ignore)
  // Method -1 to Log Error with tag


  //Method -2 gracefully terminating a stream
 /* faultySource.recover{
    case _: RuntimeException => Int.MinValue
  }.log("gracefully Recovered Source")
    .runWith(Sink.foreach[Int](println))*/

  //Method -3 recover with another stream
 /* faultySource.recoverWithRetries(3,{
    case _:RuntimeException => Source(91 to 99)
  }).log("RecoverWithRetries").runWith(Sink.foreach[Int](println))
*/
  // 4 - back off supervision
/*  import scala.concurrent.duration._
  val restartSource = RestartSource.onFailuresWithBackoff(
    min = 1 second,

  )(()=>{
    val randomNumber = new Random().nextInt(20))
    Source(1 to 10).map(elem => if (elem == randomNumber) throw new RuntimeException("failed") else elem)
  })*/

  //Supervisor Strategy
  val number = Source(1 to 20).map(e => if (e == 13) throw new RuntimeException("failed") else e)

  number.withAttributes(ActorAttributes.supervisionStrategy{
    case _:RuntimeException => Resume
    case _ => Stop
  }).runWith(Sink.foreach[Int](println))


}



