package fr.ensimag.sysd.distr_make

import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Actor
import scala.sys.process._

object Master extends App {
  val filename = "Makefile"
  val parser_makefile = new Parser(filename)
  parser_makefile.print_all_target()
  parser_makefile.create_graph()

  for (task <- parser_makefile.tasks) {
    println(task, task.target, task.parent.map(x => x.target), task.children.map(x => x.target))
  }

  
}
