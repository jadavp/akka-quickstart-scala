package com.example.UdemyDemo

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.example.UdemyDemo.VotingExe.Citizen.{Vote, VoteStatus, VoteStatusReply}
import com.example.UdemyDemo.VotingExe.VoteAggergators.VoteaAgg

object VotingExe extends App {

  object Citizen {

    case class Vote(candidate: String)

    case object VoteStatus

    case class VoteStatusReply(candidate: Option[String])

  }

  class Citizen extends Actor {

    def voted(candidate: String): Receive = {
      case VoteStatus => sender() ! VoteStatusReply(Some(candidate))
    }

    override def receive: Receive = {
      case Vote(c) => context.become(voted(c),false)
      case VoteStatus => sender() ! VoteStatusReply(None)
    }
  }


  object VoteAggergators {

    case class VoteaAgg(actorref: Set[ActorRef])

    case object VoteStatus

    case object VoteAggStatus

  }

  class VoteAggergators extends Actor {
    var datamap: Map[String, String] = Map()

    override def receive: Receive = {
      case VoteaAgg(citizen) => citizen.foreach(x => x ! VoteStatus)
        context.become(awatingstatus(citizen, Map()))
    }

    def awatingstatus(stillwaiting: Set[ActorRef], currentstatus: Map[String, Int]): Receive = {
      case VoteStatusReply(None) => sender() ! VoteStatus
      case VoteStatusReply(Some(candidate)) =>
        val newstillwaiting = stillwaiting - sender()
        val currentVotesofCandidates = currentstatus.getOrElse(candidate, 0)
        val newstats = currentstatus + (candidate -> (currentVotesofCandidates + 1))
        if (newstillwaiting.isEmpty) {
          println(s"[Aggregator current status of vote $newstats]")

        }
        else {
          context.become(awatingstatus(newstillwaiting, newstats))
        }
    }
  }


  val actorSystem = ActorSystem("VotingSystem")
  val prakash = actorSystem.actorOf(Props[Citizen], "Prakash")
  val neha = actorSystem.actorOf(Props[Citizen], "Neha")
  val ravi = actorSystem.actorOf(Props[Citizen], "Ravi")
  val pooja = actorSystem.actorOf(Props[Citizen], "Pooja")
  val voteAgg = actorSystem.actorOf(Props[VoteAggergators])

  prakash ! Vote("mummy")
  ravi ! Vote("mummy")
  neha ! Vote("papa")
  neha ! Vote("mummy")
  voteAgg ! VoteaAgg(Set(prakash, ravi, neha))


}
