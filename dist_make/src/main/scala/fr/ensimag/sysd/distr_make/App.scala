package fr.ensimag.sysd.distr_make

import scala.collection.mutable.{ListBuffer, Queue}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.rogach.scallop._
import java.net._
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import com.typesafe.config.ConfigFactory



object App {


object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
      val cluster = Cluster(ctx.system)

      if (cluster.selfMember.hasRole("Worker")) {
        val workersPerNode =
          ctx.system.settings.config.getInt("transformation.workers-per-node")
        (1 to workersPerNode).foreach { n =>
          ctx.spawn(Worker(), s"Worker$n")
        }
      }
      if (cluster.selfMember.hasRole("Master")) {
        ctx.spawn(Master(), "Master")
      }
      Behaviors.empty
    }
  }


class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  version("distributed make (c) 2020 Degola G., Giardina S., Privitera C.A., Stentzel Q.")
  banner("""Usage: make [OPTION]
           |Distributed make - Systèmes Distribués - Ensimag 3A
           |Options:
           |""".stripMargin)
  //footer("\nFor more information, consult the report")

  val filename = opt[String]("file", descr = "path to Makefile")
  val target = opt[String]("target", descr = "makefile's target to build")
  verify()
}


def main(args: Array[String]): Unit = {
  var filename = "Makefile"
  var target = ""

  // parse command line
  if(args == 2){
  val conf = new Conf(args)
  if(conf.filename.isSupplied)
    filename = conf.filename()
  if(conf.target.isSupplied)
    target = conf.target()

  val parser_makefile = new Parser(filename)
  //parser_makefile.print_all_target()
  //println(parser_makefile.tasks.map(x => x.target))
  // build graph of dependencies
  parser_makefile.create_graph(target)

  /*
  for (task <- parser_makefile.tasks.values)
    println(task, task.target, task.parent.map(x => x.target), task.children.map(x => x.target))
  println(parser_makefile.root_task.target)
  */

  // start master actor
  
  startup("master", args(0).toInt) }
  else 
  { startup("worker",arg(0).toInt) }

}

def startup(role: String, port: Int): Unit = {
    val localhost: InetAddress = InetAddress.getLocalHost
    val localIpAddress: String = localhost.getHostAddress


    // Override the configuration of the port and role
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

