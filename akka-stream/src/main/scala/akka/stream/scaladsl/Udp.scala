package akka.stream.scaladsl

import akka.stream.Attributes
import akka.stream.impl.io.SimpleUdpSink
import akka.util.ByteString

import java.net.InetSocketAddress

object Udp {
  import Attributes._

  /**
   * Creates a `Sink` which uses Akka-IO's unconnected UDP mode to emit each ByteString it receives to the given target address.
   */
  def simpleUdp(host: String, port: Int): Sink[ByteString, Unit] =
    new Sink(new SimpleUdpSink(new InetSocketAddress(host, port), none, Sink.shape("SimpleUdpSink")))
}
