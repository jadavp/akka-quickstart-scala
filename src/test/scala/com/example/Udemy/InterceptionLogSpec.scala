package com.example.Udemy

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptionLogSpec extends TestKit(ActorSystem("InterceptionLogSpec",ConfigFactory.load.getConfig("interceptlogging")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit ={
    TestKit.shutdownActorSystem(system)
  }


  import InterceptionLogSpec._
  val item = "School bag"
  val card = "1234-1234-1234-1243"
  val invalidCard = "0000-1234-1234-1243"
  "A checkout Flow" should {
    "correctly log dispatch order" in  {
      EventFilter.info(pattern = s"order [0-9]+ has been dispatched for $item",occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item,card)
      }
    }

    "freak out if payment is denied" in  {
      EventFilter[RuntimeException](occurrences = 1) intercept {
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item,invalidCard)

      }
    }
  }
}


object InterceptionLogSpec {
  case class Checkout(item: String, card: String)

  case class AutorizeCard(card: String)

  case object PaymentAccepted

  case object PaymentDenied

  case class DispatchOrder(item: String)

  case object OrderConfirmed

  class CheckoutActor extends Actor{

    private val paymeneManger = context.actorOf(Props[PaymentManager])
    private val fulfillmentManger = context.actorOf(Props[FulfillimentManager])
    override def receive: Receive = awaitingCheckout

    def awaitingCheckout :Receive = {
      case Checkout(item,card) =>
        paymeneManger ! AutorizeCard(card)
        context.become(pendingPayment(item))
    }
    def pendingPayment(item:String) : Receive ={
      case PaymentAccepted =>
        fulfillmentManger ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
        sender()
      case PaymentDenied => throw new RuntimeException("I can't handle this payment")
    }

    def pendingFulfillment (item:String) :Receive={
      case  OrderConfirmed => context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor{

    override def receive: Receive = {
      case AutorizeCard(card) =>
        Thread.sleep(3000)
        if(card.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepted
    }
  }

  class FulfillimentManager extends Actor with ActorLogging{
    var orderId:Int = 43
    override def receive: Receive = {

      case DispatchOrder(item) =>
        orderId +=1
        log.info(s"order $orderId has been dispatched for $item")
      sender() ! OrderConfirmed

    }
  }



}