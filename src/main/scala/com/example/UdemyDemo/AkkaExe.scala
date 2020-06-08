package com.example.UdemyDemo

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.example.UdemyDemo.AkkaExe.Counter.{decremented, incrementer}
import com.example.UdemyDemo.AkkaExe.Person.LiveTheLife

object AkkaExe extends App {

  object Counter {

    case class incrementer(add: Int)

    case class decremented(deduce: Int)

  }

  class Counter extends Actor {
    var value = 0

    override def receive: Receive = {
      case incrementer(add) => value += add
        println(value)
      case decremented(deduce) => value -= deduce
        println(value)
    }
  }

  val actorSyestem = ActorSystem("sampleActor")
  val counter = actorSyestem.actorOf(Props[Counter], "Counter")

  counter ! incrementer(12)
  counter ! decremented(10)


  object BankAccount {

    case class Deposit(in: Double)

    case class Withdrawal(out: Double)

    case object PrintStatement

    case class TransactionSuccess(message: String)

    case class TransactionFailure(reason: String)


  }

  class BankAccount extends Actor {
    import AkkaExe.BankAccount._
    var funds = 0.0

    override def receive: Receive = {
      case Deposit(in) =>
        if (in < 0) (sender() ! TransactionFailure(s"invalid amount $in"))
        else {
          funds += in
          (sender() ! TransactionSuccess(s"successfully deposit $in"))
        }
      case Withdrawal(out) =>
        if (out < 0) (sender() ! TransactionFailure(s"invalid amount $out"))
        else if (funds < out) {
          (sender() ! TransactionFailure(s"funds not available $out"))
        }
        else {
          funds -= out
          (sender() ! TransactionSuccess(s"successfully withdrew $out"))
        }
      case PrintStatement => println(funds)
        (sender() ! TransactionSuccess(s"Amount balance $funds"))
    }
  }

  object Person {

    case class LiveTheLife(ref: ActorRef)

  }

  class Person extends Actor {
    import AkkaExe.BankAccount._
    override def receive: Receive = {
      case LiveTheLife(account) =>
        account ! Deposit(100)
        account ! Deposit(-100)
        account ! Withdrawal(50)
        account ! PrintStatement
      case message => println(message.toString)
    }
  }

  val cashier = actorSyestem.actorOf(Props[BankAccount], "Cashier")
  val person = actorSyestem.actorOf(Props[Person],"Rich")

  person ! LiveTheLife(cashier)

}
