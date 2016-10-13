/**
 * Copyright (C) 2014-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.scaladsl

import java.net.InetSocketAddress

import akka.actor.{ Actor, ActorRef, Props }
import akka.io.{ IO, Udp ⇒ IOUdp }
import akka.stream.ActorMaterializer
import akka.stream.testkit.{ StreamSpec, TestUtils }
import akka.testkit.TestProbe
import akka.util.ByteString

class SimpleUdpSinkSpec extends StreamSpec {

  implicit val materializer = ActorMaterializer()

  "A Flow with Udp.simpleUdp" must {
    val addr = TestUtils.temporaryServerAddress(udp = true)
    val serverProbe = createUdpServer(addr)

    val Hello = ByteString("Hello")
    val World = ByteString("World")
    val ExclamationMark = ByteString("!")
    val words = Source(List(Hello, World, ExclamationMark))

    "send all received ByteStrings using UDP" in {
      words.runWith(Udp.simpleUdp(addr.getAddress.getHostAddress, addr.getPort))

      serverProbe.expectMsg(Hello)
      serverProbe.expectMsg(World)
      serverProbe.expectMsg(ExclamationMark)
    }

  }

  def createUdpServer(addr: InetSocketAddress) = {
    val p = TestProbe()
    system.actorOf(Props(new ServerActor(addr, p)).withDispatcher("akka.test.stream-dispatcher"))
    p.expectMsg("init-done")
    p
  }

  class ServerActor(addr: InetSocketAddress, p: TestProbe) extends Actor {
    IO(IOUdp)(context.system) ! IOUdp.Bind(self, addr)

    def receive = {
      case IOUdp.Bound(local) ⇒
        p.ref ! "init-done"
        context.become(ready(sender()))
    }

    def ready(socket: ActorRef): Receive = {
      case IOUdp.Received(data, remote) ⇒ p.ref ! data
      case IOUdp.Unbind                 ⇒ socket ! IOUdp.Unbind
      case IOUdp.Unbound                ⇒ context.stop(self)
    }
  }

}
