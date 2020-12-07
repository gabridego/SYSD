package fr.ensimag.sysd.distr_make

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.rogach.scallop._


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


case class InitMake(parser: Parser)

case class MakeTask()
case class CompletedDep()
case class ErrorDep()
case class TaskMade(task: Task)
case class InitWorker(task: Task, toContact: List[ActorRef])

/** Master actor.
 *
 *  Initialize a worker for the root task, calls a recursive function and waits for completion
 */
class Master extends Actor {
  var stack = Map[String, ActorRef]()

  /** Initialize a worker for each task of the given list
   *
   *  Recursive function, calls itself for the tasks that depend on the current one.
   *
   *  @param tasks
   */
  private def recMake(tasks: List[Task]): Unit = {
    var goodTasks = Set[Task]()

    for (task <- tasks) {
      // consider a task only if new
      if (!(stack contains task.target)) {
        val worker = context.actorOf(Props[Worker], name = task.target)

        // send message to worker, with task to build and list of workers to notify (the parents)
        worker ! InitWorker(task, task.parent.map(x => stack(x.target)))
        // store mapping between target and worker
        stack = stack + (task.target -> worker)

        goodTasks += task
      }
    }

    for (task <- tasks) {
      if (goodTasks contains task) {
        // recursively call workers for children
        recMake(task.children)
      }
    }
  }

  def receive = {
    // initialization message, create worker for root task
    case InitMake(parser) =>
      val root = parser.root_task
      val worker =  context.actorOf(Props[Worker], name = root.target)
      worker ! InitWorker(root, List(self))
      stack = stack + (root.target -> worker)

      recMake(root.children)

    // message received when root task is built
    case CompletedDep() =>
      println("make completed")
      context.system.terminate()

    case ErrorDep() =>
      println("compilation error!")
      context.system.terminate()
  }
}

/** Worker actor
 *
 *  Receives information for a task and runs the command when all dependencies are satisfied
 */
class Worker extends Actor {
  var curTask: Task = _
  var leftDep: Int = _                      // remaining dependencies to satisfy
  var taskStarted: Boolean = false          // flag to ensure command is run only once
  var actorsToContact: List[ActorRef] = _   // list of parent workers

  def receive = {
    case InitWorker(task, toContact) =>
      curTask = task
      leftDep = task.children.size
      actorsToContact = toContact
      if (leftDep <= 0)
        self ! MakeTask()

    // message received when all dependencies are satisfied, contacts parent after execution
    case MakeTask() =>
      if (!taskStarted) {
        taskStarted = true
        if (CommandRunner.run(curTask.command) == 0) {
          for (actor <- actorsToContact)
            actor ! CompletedDep()
        } else {
          for (actor <- actorsToContact)
            actor ! ErrorDep()
        }
      }

    // message received when one of the children is correctly built, decreases count of dependencies
    // to satisfy, possibly calls execution
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
  var filename = "Makefile"
  var target = ""

  // parse command line
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
  */

  // start master actor
  val system = ActorSystem("distributed-make")
  val m = system.actorOf(Props[Master], name="master")
  m ! InitMake(parser_makefile)

}