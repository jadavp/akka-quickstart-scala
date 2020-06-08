package com.example.UdemyDemo

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.dispatch.{ControlMessage, PriorityGenerator, UnboundedPriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

object MailboxDemo extends App {
  val system = ActorSystem("MailBoxDemo",ConfigFactory.load().getConfig("mailboxesDemo"))
  class SimpleActor extends Actor with ActorLogging {
    override def receive:Receive ={
      case message:String => log.info(message)
      case ManagementMessage => log.info("MangementMessage")
    }
  }

  //Step 1 Mailbox Definition
  class SupportTicketProrityMailBox(setting:ActorSystem.Settings,config:Config)
    extends UnboundedPriorityMailbox(
      PriorityGenerator{
        case message: String if (message.startsWith("[P0]")) => 0
        case message: String if (message.startsWith("[P1]")) => 1
        case message: String if (message.startsWith("P2]")) => 2
        case message: String if (message.startsWith("[P3]")) => 3
        case _ => 4
      }
    )

  //Step 2 - make it known in config
  //step 3 = attach dispatcher in an actor

  val supportTicketLogger = system.actorOf(Props[SimpleActor].withDispatcher("support-ticket-dispatcher"))
 /* supportTicketLogger ! PoisonPill
  Thread.sleep(1000)
  supportTicketLogger ! "[P3] Nice to solve this"
  supportTicketLogger ! "[P0] Urgent Priority"
  supportTicketLogger ! "[P1] Important to solve"*/

  case object ManagementMessage extends ControlMessage

  val controlMessageActor = system.actorOf(Props[SimpleActor].withMailbox("control-mailbox"))
  /*controlMessageActor ! "[P3] Nice to solve this"
  controlMessageActor ! "[P0] Urgent Priority"
  controlMessageActor ! "[P1] Important to solve"
  controlMessageActor ! ManagementMessage*/

  val altControlActor = system.actorOf(Props[SimpleActor],"altControlActor")
  altControlActor ! "[P3] Nice to solve this"
  altControlActor ! "[P0] Urgent Priority"
  altControlActor ! "[P1] Important to solve"
  altControlActor ! ManagementMessage



}
