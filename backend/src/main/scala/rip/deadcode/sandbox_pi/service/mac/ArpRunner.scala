package rip.deadcode.sandbox_pi.service.mac

import cats.effect.IO
import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import rip.deadcode.sandbox_pi.service.mac.ArpRunnerOutput
import rip.deadcode.sandbox_pi.service.mac.ArpRunnerOutput.Item
import rip.deadcode.sandbox_pi.service.mac.ArpRunner.{IpRegex, MacRegex}

import scala.collection.mutable.ListBuffer

@Singleton
class ArpRunner @Inject() () {

  private val logger = LoggerFactory.getLogger(classOf[ArpRunner])

  def run(): IO[ArpRunnerOutput] = IO.blocking {
    import sys.process.*
    val stdout = ListBuffer[String]()
    val stderr = ListBuffer[String]()
    val pl = ProcessLogger(stdout.append, stderr.append)
    // https://superuser.com/a/261823
    val status = "arp-scan --interface=eth0 --localnet --plain" ! pl

    logger.debug("Arp result\n{}", stdout)

    if (status != 0) {
      logger.warn("Failed to execute arp process: {}\n{}", status, stderr.mkString("\n"))
      throw new RuntimeException()
    } else {
      val items = stdout.flatMap { line =>
        val ip = IpRegex.findFirstIn(line)
        val mac = MacRegex.findFirstIn(line)
        (ip, mac) match {
          case (Some(ip), Some(mac)) => Some(Item(ip = ip, mac = mac.toLowerCase))
          case _                     => None
        }
      }.toSeq
      ArpRunnerOutput(items)
    }
  }
}

object ArpRunner {
  // https://stackoverflow.com/a/36760050
  private val IpRegex = "((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}".r

  // https://stackoverflow.com/a/4260512
  private val MacRegex = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})".r
}
