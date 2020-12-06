package fr.ensimag.sysd.distr_make

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.sys.process._

case class InitMake(parser: Parser)

case class MakeTask()
case class CompletedDep()
case class ErrorDep()
case class TaskMade(task: Task)
case class InitWorker(task: Task, toContact: List[ActorRef])

class Master extends Actor {
  var stack = Map[String, ActorRef]()

  private def recMake(tasks: List[Task]): Unit = {
    var goodTasks = Set[Task]()

    for (task <- tasks) {
      if (!(stack contains task.target)) {
        val worker = context.actorOf(Props[Worker], name = task.target)

        worker ! InitWorker(task, task.parent.map(x => stack(x.target)))
        stack = stack + (task.target -> worker)

        goodTasks += task
      }
    }

    for (task <- tasks) {
      if (goodTasks contains task)
        recMake(task.children)
    }
  }

  def receive = {
    case InitMake(parser) =>
      val root = parser.root_task
      val worker =  context.actorOf(Props[Worker], name = root.target)
      worker ! InitWorker(root, List(self))
      stack = stack + (root.target -> worker)

      recMake(root.children)

    case CompletedDep() =>
      println("make completed")

    case ErrorDep() =>
      println("compilation error!")
  }
}

class Worker extends Actor {
  var curTask: Task = _
  var leftDep: Int = _
  var taskStarted: Boolean = false
  var actorsToContact: List[ActorRef] = _

  def receive = {
    case InitWorker(task, toContact) =>
      curTask = task
      leftDep = task.children.size
      actorsToContact = toContact
      if (leftDep <= 0)
        self ! MakeTask()

    case MakeTask() =>
      if (taskStarted == false) {
        taskStarted = true
        if (CommandRunner.run(curTask.command) == 0) {
          for (actor <- actorsToContact)
            actor ! CompletedDep()
        } else {
          for (actor <- actorsToContact)
            actor ! ErrorDep()
        }
      }

    case CompletedDep() =>
      leftDep = leftDep - 1
      if (leftDep <= 0)
        self ! MakeTask()

    case ErrorDep() =>
      for (actor <- actorsToContact)
        actor ! ErrorDep()
  }
}

object Main extends App {
  val filename = "Makefile"
  val parser_makefile = new Parser(filename)
  //parser_makefile.print_all_target()
  //println(parser_makefile.tasks.map(x => x.target))
  parser_makefile.create_graph("")

  for (task <- parser_makefile.tasks) {
    println(task, task.target, task.parent.map(x => x.target), task.children.map(x => x.target))
  }

  val system = ActorSystem("distributed-make")
  val m = system.actorOf(Props[Master], name="master")
  m ! InitMake(parser_makefile)
}