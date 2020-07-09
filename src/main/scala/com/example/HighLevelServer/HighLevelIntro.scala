package com.example.HighLevelServer

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object HighLevelIntro extends App {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer
  import system.dispatcher

  //directives


  val simpleRoute: Route =
    path("home"){ //Directive
      complete(StatusCodes.OK)  //Directive
    }

  val pathGetRoute: Route =
  path("home"){
    get{
      complete(StatusCodes.OK)
    }
  }


  //chaining directive with ~

  val chainedRoute:Route =
    path("myEndPoint"){
      get{
        complete(StatusCodes.OK)
      } ~ /* Very Important*/
      post{
        complete(StatusCodes.Forbidden)
      }
    } ~
  path("home"){
   complete(
     HttpEntity(
       ContentTypes.`text/html(UTF-8)`,
       """
      |<html>
      |<body>
      | Hello from Akka HTTP!
      |</body>
      |</html>
      |""".stripMargin
     )
   )
  }

  Http().bindAndHandle(chainedRoute,"localhost",8080)



}
