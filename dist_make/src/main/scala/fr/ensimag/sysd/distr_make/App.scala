package fr.ensimag.sysd.distr_make

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory
import java.net._
import TimerSingleton._

object App {

  object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
      val cluster = Cluster(ctx.system)

      startCreationActor()

      if (cluster.selfMember.hasRole("worker")) {
        val workersPerNode =
          ctx.system.settings.config.getInt("transformation.workers-per-node")
        (1 to workersPerNode).foreach { n =>
          ctx.spawn(Worker(), s"Worker$n")
        }
      }
      if (cluster.selfMember.hasRole("master")) {
        ctx.spawn(Master(), "Master")
      }

      startCreationParsingTreeAndStopActorCreation()

      Behaviors.empty
    }
  }

  def main(args: Array[String]): Unit = {
    // starting 1 master node and 2 worker nodes
    if (args.isEmpty) {
      startup("worker", 25251)
      startup("worker", 25252)
      startup("master", 0)
    } else {
      require(args.length == 2, "Usage: role port")
      startup(args(0), args(1).toInt)
    }
  }

  def startup(role: String, port: Int): Unit = {
    val localhost: InetAddress = InetAddress.getLocalHost
    val localIpAddress: String = localhost.getHostAddress

    val config = ConfigFactory
      .parseString(s"""
        akka.remote.artery.canonical.port=$port
        akka.cluster.roles = [$role]
        akka.remote.artery.canonical.hostname=$localIpAddress
        """)
      .withFallback(ConfigFactory.load("transformation"))

    ActorSystem[Nothing](RootBehavior(), "ClusterSystem", config)

  }

}
