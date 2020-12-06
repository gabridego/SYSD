package fr.ensimag.sysd.distr_make

import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Actor
import scala.sys.process._

case class StartMake(depTree: Tree, maxWorkers: Int)
case class MakeTask(task: Task)
case class TaskMade(task: Task)

class Master extends Actor {
  def receive = {
    case StartMake(depTree, maxWorkers) =>
      for (leaf <- depTree.leaves) {
        //create and store workers, start leaves
        //MakeTask
      }

    case TaskMade(task) =>

  }
}

class Worker extends Actor {
  def receive = {
    case MakeTask(task) =>
      for (cmd <- task.command.split(";"))
        //execute cmd
      sender ! TaskMade(task)
  }
}

object Main extends App {
  val max_workers = 10
  val filename = "Makefile"
  val parser_makefile = new Parser(filename)
  parser_makefile.print_all_target()
  parser_makefile.create_graph()

  for (task <- parser_makefile.tasks) {
    println(task, task.target, task.parent.map(x => x.target), task.children.map(x => x.target))
  }

  val system = ActorSystem("distributed-make")
  val m = system.actorOf(Props[Master], name="master")
  m ! StartMake(new Tree, max_workers)
}