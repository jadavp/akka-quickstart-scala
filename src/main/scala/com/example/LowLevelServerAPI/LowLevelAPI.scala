package com.example.LowLevelServerAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future

object LowLevelAPI extends App {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  val serverSource = Http().bind("localhost", 8000)
  val connectionSink = Sink.foreach[IncomingConnection] { conection =>
    println(s"Accepted incoming connection from :${conection.remoteAddress}")
  }

  //val serverBindingFuture = serverSource.to(connectionSink).run()
  /* serverBindingFuture.onComplete {
    case Success(binding) =>
      println(s"server binding succesful: $binding")
      binding.terminate(2.second)
    case Failure(ex) => println(s"Server binding failed: $ex")
  }*/

  // Method 1: Synchornously server HTTP responses

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity =
          HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Hello from Akka HTTP!
            |</body>
            |</html>
            |""".stripMargin)
      )
    case request: HttpRequest =>
      HttpResponse(
        StatusCodes.NotFound,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            | OOPs! THe resources cant be found
            |</body>
            |</html>
            |""".stripMargin
        )
      )
  }

  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] {
    connection =>
      connection.handleWithSyncHandler(requestHandler)
  }

  //Http().bind("localhost", 8080).runWith(httpSyncConnectionHandler)
  //Http().bindAndHandleSync(requestHandler, "localhost", 8080)

  // Method 2: ASynchornously server HTTP responses
  import system.dispatcher
  val AsyncrequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), _, _, _) =>
      Future(
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Hello from Akka HTTP!
            |</body>
            |</html>
            |""".stripMargin)
        )
      )
    case request: HttpRequest =>
      Future(
        HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
            |<html>
            |<body>
            | OOPs! THe resources cant be found
            |</body>
            |</html>
            |""".stripMargin
          )
        )
      )
  }

  val httpAsyncConnectionHandler = Sink.foreach[IncomingConnection] {
    connection =>
      connection.handleWithAsyncHandler(AsyncrequestHandler)
  }

  //Http().bind("localhost", 8081).runWith(httpAsyncConnectionHandler)
  //Http().bindAndHandleAsync(AsyncrequestHandler, "localhost", 8080)

//Method 3 : Async with akka Stream

  val streanbasedrequestHandler: Flow[HttpRequest, HttpResponse, _] =
    Flow[HttpRequest].map {
      case HttpRequest(HttpMethods.GET, _, _, _, _) =>
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Hello from Akka HTTP!
            |</body>
            |</html>
            |""".stripMargin)
        )
      case request: HttpRequest =>
        HttpResponse(
          StatusCodes.NotFound,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
            |<html>
            |<body>
            | OOPs! THe resources cant be found
            |</body>
            |</html>
            |""".stripMargin
          )
        )
    }

  Http()
    .bind("localhost", 8082)
    .runForeach(connection => connection.handleWith(streanbasedrequestHandler))

  val httpBinding = Http().bindAndHandle(streanbasedrequestHandler, "localhost", 8082)
  httpBinding.flatMap(binding => binding.unbind()).onComplete(_=>system.terminate())

}
