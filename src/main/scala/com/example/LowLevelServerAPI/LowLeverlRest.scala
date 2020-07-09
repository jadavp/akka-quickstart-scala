package com.example.LowLevelServerAPI

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.LowLevelServerAPI.GuitarDB._
import spray.json._
import scala.concurrent.Future
import scala.concurrent.duration._

case class Guitar(make: String, model: String, quantity: Int)

object GuitarDB {

  case class CreateGuitar(guitar: Guitar)

  case class GuitarCreated(id: Int)

  case class FindGuitar(id: Int)

  case object FindAllGuitar

  case class AddQuantity(id: Int, quantity: Int = 0)

  case class FindGuitarsInStock(inStock: Boolean)

}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  var guitars: Map[Int, Guitar] = Map()
  var currentGuitarId: Int = 0

  override def receive: Receive = {
    case FindAllGuitar =>
      log.info("seraching for all Guitars")
      sender() ! guitars.values.toList

    case FindGuitar(id) =>
      log.info(s"seraching for guitar : $id")
      sender() ! guitars.get(id)
    case CreateGuitar(guitar) =>
      log.info(s"Adding guitar$guitar with id $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1
    case AddQuantity(id, quantity) =>
      log.info(s"Trying to add $quantity for guitar $id")
      val guitar: Option[Guitar] = guitars.get(id)
      val newGuitar: Option[Guitar] = guitar.map {
        case Guitar(make, model, q) => Guitar(make, model, quantity + q)
      }
      newGuitar.foreach {
        guitar => guitars = guitars + (id -> guitar)
      }
      sender() ! newGuitar

    case FindGuitarsInStock(inStock) =>
      log.info(s"Serching for all guitars ${if (inStock) "in" else "out of"}stock")
      if (inStock)
        sender() ! guitars.values.filter(_.quantity > 0)
      else
        sender() ! guitars.values.filter(_.quantity == 0)


  }

}

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat = jsonFormat3(Guitar) //jsonFormatx => Where x is number of member in case class
}

object LowLeverlRest extends App with GuitarStoreJsonProtocol {
  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  //Json -> marshelling
  val simpleGuitar = Guitar("Fender", "prakash", 1)
  println(simpleGuitar.toJson.prettyPrint)

  //unmarshelling
  val simpleGuitarJsonString =
    """{
      |  "make": "Fender",
      |  "model": "prakash",
      |  "quantity": 1
      |}
      |""".stripMargin
  println(simpleGuitarJsonString.parseJson.convertTo[Guitar])

  val guitarDB = system.actorOf(Props[GuitarDB], "LowlelGuitarDB")

  val guitarlist = List(
    Guitar("Fender", "Stratocaster", 0),
    Guitar("Gibson", "Les Paul", 3),
    Guitar("Martin", "LX1", 4)
  )

  guitarlist.foreach { guitar =>
    guitarDB ! CreateGuitar(guitar)
  }

  import system.dispatcher

  implicit val defaultTimeOut = Timeout(2.second)

  def getGuitar(query: Query): Future[HttpResponse] = {

    val guitarID = query.get("Id").map(_.toInt)
    guitarID match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(name: Int) =>
        val guitarFuture: Future[Option[Guitar]] = (guitarDB ? FindGuitar(name)).mapTo[Option[Guitar]]
        guitarFuture.map {
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(guitar: Guitar) => HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitar.toJson.prettyPrint
            )
          )
        }
    }
  }

  def getGuitarQuantity(query: Query): Future[HttpResponse] = {

    val guitarID = query.get("inStock").map(_.toBoolean)
    guitarID match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(flag: Boolean) =>
        val guitarFuture: Future[List[Guitar]] =
          (guitarDB ? FindGuitarsInStock(flag)).mapTo[List[Guitar]]
        guitarFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
    }
  }


  def postguitar(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    val guitarquantity = query.get("quantity").map(_.toInt)
    val validGuitarResponseFuture: Option[Future[HttpResponse]] = for {id <- guitarId
                                                                       quantity <- guitarquantity} yield {
      val newGuitarFuture: Future[Option[Guitar]] = (guitarDB ? AddQuantity(id, quantity)).mapTo[Option[Guitar]]
      newGuitarFuture.map(_ => HttpResponse(StatusCodes.OK))
    }
    validGuitarResponseFuture match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(response) => response
    }
  }

  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri@Uri.Path("/api/guitar"), _, _, _) =>

      val query = Uri.query()
      if (query.isEmpty) {
        val guitarFuture: Future[List[Guitar]] =
          (guitarDB ? FindAllGuitar).mapTo[List[Guitar]]
        guitarFuture.map { guitars =>
          HttpResponse(
            entity = HttpEntity(
              ContentTypes.`application/json`,
              guitars.toJson.prettyPrint
            )
          )
        }
      }
      else {
        getGuitar(query)
      }

    case HttpRequest(HttpMethods.GET, Uri@Uri.Path("/api/guitar/inventory"), _, _, _) =>
      val query = Uri.query()
      if (query.isEmpty) {
        Future(HttpResponse(StatusCodes.NotFound))
      }
      else {
        getGuitarQuantity(query)
      }

    case HttpRequest(HttpMethods.POST, Uri@Uri.Path("/api/guitar/inventory"), _, entity: HttpEntity, _) =>
      val query = Uri.query()
      if (query.isEmpty) {
        Future(HttpResponse(StatusCodes.NotFound))
      } else {
        postguitar(query)
      }


    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitar"), _, entity: HttpEntity, _) =>
      val strictEntityFuture = entity.toStrict(3.second)
      strictEntityFuture.flatMap { strictEntity =>
        val guitarJSonString =
          strictEntity.data.utf8String.parseJson.convertTo[Guitar]
        val guitarCreatedFuture: Future[GuitarCreated] =
          (guitarDB ? CreateGuitar(guitarJSonString)).mapTo[GuitarCreated]
        guitarCreatedFuture.map { guitarcreated =>
          HttpResponse(StatusCodes.OK)
        }
      }
    case request: HttpRequest =>
      request.discardEntityBytes()
      Future {
        HttpResponse(status = StatusCodes.NotFound)
      }
  }

  Http().bindAndHandleAsync(requestHandler, "localhost", 8080)

}
