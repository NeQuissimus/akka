package akka.stream.javadsl

import akka.stream.scaladsl.{ Udp ⇒ ScalaUdp }
import akka.util.ByteString

object Udp {
  /**
   * Creates a `Sink` which uses Akka-IO's unconnected UDP mode to emit each ByteString it receives to the given target address.
   */
  def simpleUdp(host: String, port: Int): Sink[ByteString, Unit] =
    new Sink(ScalaUdp.simpleUdp(host, port))
}
