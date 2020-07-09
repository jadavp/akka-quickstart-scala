package com.example.LowLevelServerAPI

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.LowLevelServerAPI.OfferDB._
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._


case class Offer(product: String, description: String, price: Int, validdate: Int)

class OfferDB extends Actor with ActorLogging {
  var offerMap: Map[Int, Offer] = Map()
  var currentID: Int = 0

  override def receive: Receive = {
    case CreateOffer(offer) =>
      log.info(s"$offer added with ID $currentID")
      offerMap = offerMap + (currentID -> offer)
      sender() ! OfferCreated(currentID)
      currentID += 1
    case RetrieveOffer(id) =>
      log.info(s"finding offer with $id")
      val offer = offerMap.get(id).filter(_.validdate < 20)
      sender() ! offer
    case DeleteOffer(id) =>
      offerMap = offerMap.removed(id)
      sender() ! DeleteSucess(id)
    case RetrieveAllOffer =>
      val offers = offerMap.values.filter(_.validdate < 20).toList
      sender ! offers
  }

}

object OfferDB {

  case class CreateOffer(offer: Offer)

  case class RetrieveOffer(id: Int)

  case class OfferCreated(id: Int)

  case class DeleteOffer(id: Int)

  case class DeleteSucess(id: Int)

  case object RetrieveAllOffer

}

trait OffersJsonProtocol extends DefaultJsonProtocol {
  implicit val offerFormat = jsonFormat4(Offer) //jsonFormatx => Where x is number of member in case class
}

object LowLevelAPIOfferService extends App with OffersJsonProtocol {

  implicit val system = ActorSystem("SimpleActorSystem")
  implicit val materializer = ActorMaterializer

  import system.dispatcher

  val offerDB = system.actorOf(Props[OfferDB], "OfferDB")

  offerDB ! CreateOffer(Offer("Shampoo", "Buy 2 get 2", 320, 1))
  offerDB ! CreateOffer(Offer("Shop", "Buy 3 get 1", 20, 1))
  implicit val timeout = Timeout(2.seconds)
  val Offerhandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/offers"), _, _, _) =>
      val offers: Future[List[Offer]] = (offerDB ? RetrieveAllOffer).mapTo[List[Offer]]
      offers.map { offers =>
        HttpResponse(
          entity = HttpEntity(
            ContentTypes.`application/json`,
            offers.toJson.prettyPrint
          )
        )
      }
    case HttpRequest(HttpMethods.GET, Uri@Uri.Path("/offer"), _, _, _) =>
      val query = Uri.query()
      if (query.isEmpty) {
        Future(HttpResponse(StatusCodes.NotFound))
      }
      else {
        val id = query.get("id").map(_.toInt)
        id match {
          case None => Future(HttpResponse(StatusCodes.NotFound))
          case Some(id) =>
            val offerOption: Future[Option[Offer]] = (offerDB ? RetrieveOffer(id)).mapTo[Option[Offer]]
            offerOption.map {
              case None => HttpResponse(StatusCodes.NotFound)
              case Some(offer: Offer) => HttpResponse(
                entity = HttpEntity(
                  ContentTypes.`application/json`,
                  offer.toJson.prettyPrint
                )
              )
            }
        }
      }

    case HttpRequest(HttpMethods.POST, Uri@Uri.Path("/offer"), _, entity: HttpEntity, _) =>
      val strictEntityFuture = entity.toStrict(3.second)
      strictEntityFuture.flatMap { strictEntity =>
        val offerJsonString = strictEntity.data.utf8String.parseJson.convertTo[Offer]
        val offerCreatedFuture = (offerDB ? CreateOffer(offerJsonString)).mapTo[OfferCreated]
        offerCreatedFuture.map { guitarcreated =>
          HttpResponse(StatusCodes.OK)

        }
      }

    case HttpRequest(HttpMethods.DELETE, Uri@Uri.Path("/offer"), _, _, _) =>
      val query = Uri.query()
      if (query.isEmpty) {
        Future(HttpResponse(StatusCodes.NotFound))
      }
      else {
        val id = query.get("id").map(_.toInt)
        id match {
          case None => Future(HttpResponse(StatusCodes.NotFound))
          case Some(id) =>
            val offerOption: Future[DeleteSucess] = (offerDB ? DeleteOffer(id)).mapTo[DeleteSucess]
            offerOption.map { guitarcreated =>
              HttpResponse(StatusCodes.OK)
            }
        }
      }
  }

  Http().bindAndHandleAsync(Offerhandler, "localhost", 8080)

}
