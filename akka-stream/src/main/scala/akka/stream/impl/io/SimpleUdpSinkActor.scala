/**
 * Copyright (C) 2014-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package akka.stream.impl.io

import java.net.InetSocketAddress

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.io.{ IO, Udp }
import akka.stream.actor.ActorSubscriberMessage.{ OnComplete, OnError, OnNext }
import akka.stream.actor.{ ActorSubscriber, RequestStrategy, WatermarkRequestStrategy, ZeroRequestStrategy }
import akka.util.ByteString

/** INTERNAL API */
private[akka] object SimpleUdpSinkActor {
  def props(target: InetSocketAddress): Props =
    Props(new SimpleUdpSinkActor(target))
}

/** INTERNAL API */
private[akka] class SimpleUdpSinkActor(target: InetSocketAddress) extends ActorSubscriber with ActorLogging {
  private[this] var currentRequestStrategy: RequestStrategy = ZeroRequestStrategy
  override protected def requestStrategy = currentRequestStrategy

  import context.system

  IO(Udp) ! Udp.SimpleSender

  override def receive: Receive = {
    case Udp.SimpleSenderReady ⇒
      currentRequestStrategy = WatermarkRequestStrategy(10) // TODO arbitrary value, how can we do better here (needs dropping to low level)
      context.become(ready(sender()))
    case OnComplete ⇒ context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Shutting down (targetted at: {}", target)
      context.stop(self)
  }

  def ready(targetRef: ActorRef): Receive = {
    case OnNext(bytes: ByteString) ⇒ targetRef ! Udp.Send(bytes, target)
    case OnComplete                ⇒ context.stop(self)
    case OnError(cause) ⇒
      log.error(cause, "Shutting down (targetted at: {})", target)
      context.stop(self)
  }
}