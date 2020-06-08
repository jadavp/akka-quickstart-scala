//#full-example
package com.example.Udemy

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.example.UdemyDemo.Greeter
import com.example.UdemyDemo.Greeter.{Greet, Greeted}
import org.scalatest.wordspec.AnyWordSpecLike

//#definition
class AkkaQuickstartSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
//#definition

  "A Greeter" must {
    //#test
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
    //#test
  }

}
//#full-example
