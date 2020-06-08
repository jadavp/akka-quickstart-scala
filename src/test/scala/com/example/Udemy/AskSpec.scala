package com.example.Udemy


import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.example.Udemy.AskSpec.{AuthManager, AuthManger}
import com.example.Udemy.AskSpec.AuthManger.{AuthFailure, Authenicate}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class AskSpec extends TestKit(ActorSystem("BasicSpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "An Authenicator" should {
    import AuthManger._
    "fail to authenicate a non-register user" in{
      val  authManager = system.actorOf(Props[AuthManager])
      authManager ! Authenicate("prakash","ilun")
      expectMsg(AuthFailure(AUTH_FAILURE_NOT_FOUND))

    }

    "fail to authenticate if password is wrong" in {
      val  authManager = system.actorOf(Props[AuthManager])
      authManager ! RegisterUser("prakash","ilun")
      authManager ! Authenicate("prakash","ilur")
      expectMsg(AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT))
    }
  }



}


object AskSpec {

  case class Read(key: String)

  case class Write(Key: String, value: String)

  class KVActor extends Actor with ActorLogging {

    override def receive: Receive = online(Map())

    def online(kv: Map[String, String]): Receive = {
      case Read(key) =>
        log.info(s"Tryting to read value at the key : $key")
        sender() ! kv.get(key)
      case Write(key, value) =>
        log.info(s"Writing value $value value at key $key")
        context.become(online(kv + (key -> value)))
    }

  }


  object AuthManger {
    val AUTH_FAILURE_NOT_FOUND = "Username not Found"

    val AUTH_FAILURE_PASSWORD_INCORRECT = "Password incorrect"

    val SYSTEM_ERROR = "System Error"

    case class RegisterUser(username: String, password: String)

    case class Authenicate(username: String, password: String)

    case class AuthFailure(message: String)

    case object AuthSucess

  }


  class AuthManager extends Actor with ActorLogging {

    import AuthManger._

    implicit val timeout: Timeout = Timeout(1.seconds)
    implicit val executionContext: ExecutionContext = context.dispatcher

    private val authDB = context.actorOf(Props[KVActor])

    override def receive: Receive = {
      case RegisterUser(username, password) => authDB ! Write(username, password)
      case Authenicate(username, password) =>
        val originalSender = sender()
        val future = authDB ? Read(username)
        future.onComplete {
          // NEVER CALL Methods on the actor 9 ot access mutanle state in oncomplete
          case Success(None) => originalSender ! AuthFailure(AUTH_FAILURE_NOT_FOUND)
          case Success(Some(dbPassword)) =>
            if (dbPassword == password) originalSender ! AuthSucess
            else sender() ! AuthFailure(AUTH_FAILURE_PASSWORD_INCORRECT)
          case Failure(_) => originalSender ! AuthFailure(SYSTEM_ERROR)
        }
    }
  }

}