package com.example.LowLevelServerAPI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

object LowLevelAPIExe extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  val syncRequest: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          """
            |<html>
            |<body>
            | This website was created by Datasavioud and it is about infomration
            |</body>
            |</html>
            |
            |""".stripMargin
        )
      )
    case HttpRequest(HttpMethods.GET, Uri.Path("/search"), _, _, _) =>
      HttpResponse(
        StatusCodes.Found,
        headers = List(Location("http://google.com"))
      )
    case HttpRequest(HttpMethods.GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(
        StatusCodes.OK,
        entity =
          HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Welcome to the website
            |</body>
            |</html>
            |
            |""".stripMargin)
      )
    case request: HttpRequest =>
      HttpResponse(
        StatusCodes.NotFound,
        entity =
          HttpEntity(ContentTypes.`text/html(UTF-8)`, """
           |<html>
           |<body>
           | Resources not found
           |</body>
           |</html>
           |
           |""".stripMargin)
      )
  }
  Http().bindAndHandleSync(syncRequest, "localhost", 8388)
  import system.dispatcher
  val asyncRequest: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) =>
      Future(
        HttpResponse(
          StatusCodes.OK,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
            |<html>
            |<body>
            | This website was created by Datasavioud and it is about infomration
            |</body>
            |</html>
            |
            |""".stripMargin
          )
        )
      )

    case HttpRequest(HttpMethods.GET, _, _, _, _) =>
      Future(
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Welcome to the website
            |</body>
            |</html>
            |
            |""".stripMargin)
        )
      )

    case request: HttpRequest =>
      Future(
        HttpResponse(
          StatusCodes.NotFound,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
            |<html>
            |<body>
            | Resources not found
            |</body>
            |</html>
            |
            |""".stripMargin)
        )
      )
  }

  //Http().bindAndHandleAsync(asyncRequest, "localhost", 8388)

  val flowBasedRequest: Flow[HttpRequest, HttpResponse, _] =
    Flow[HttpRequest].map {
      case HttpRequest(HttpMethods.GET, Uri.Path("/about"), _, _, _) =>
        HttpResponse(
          StatusCodes.OK,
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            """
              |<html>
              |<body>
              | This website was created by Datasavioud and it is about infomration
              |</body>
              |</html>
              |
              |""".stripMargin
          )
        )

      case HttpRequest(HttpMethods.GET, _, _, _, _) =>
        HttpResponse(
          StatusCodes.OK,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
              |<html>
              |<body>
              | Welcome to the website
              |</body>
              |</html>
              |
              |""".stripMargin)
        )

      case request: HttpRequest =>
        HttpResponse(
          StatusCodes.NotFound,
          entity =
            HttpEntity(ContentTypes.`text/html(UTF-8)`, """
              |<html>
              |<body>
              | Resources not found
              |</body>
              |</html>
              |
              |""".stripMargin)
        )
    }

  //Http().bindAndHandle(flowBasedRequest, "localhost", 8388)
}
