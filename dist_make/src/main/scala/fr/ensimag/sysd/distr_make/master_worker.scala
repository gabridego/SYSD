package fr.ensimag.sysd.distr_make

import scala.collection.mutable.{ListBuffer, Queue}
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
 *  Initialize a worker for the root task and for the next according to a queue
 */
class Master extends Actor {
  var actorMap = Map[String, ActorRef]()
  var taskQueue = Queue[Task]()
  var waitingTasks = ListBuffer[Task]()
  var exiting: Boolean = false

  def receive = {
    // initialization message, create workers for each tasks
    case InitMake(parser) =>
      println("initializing " + self.path.name + " actor...")

      val root = parser.root_task
      val worker =  context.actorOf(Props[Worker], name = root.target)
      worker ! InitWorker(root, List(self))
      actorMap = actorMap + (root.target -> worker)

      // add root's dependencies to a queue
      taskQueue ++= root.children

      while (taskQueue.nonEmpty || waitingTasks.nonEmpty) {

        for (waitingTask <- waitingTasks) {
          if (waitingTask.parent.forall(x => actorMap contains x.target)) {
            val worker = context.actorOf(Props[Worker], name = waitingTask.target)
            worker ! InitWorker(waitingTask, waitingTask.parent.map(x => actorMap(x.target)))
            actorMap = actorMap + (waitingTask.target -> worker)

            // add dependencies to a queue only if not present yet
            for (child <- waitingTask.children)
              if (!(actorMap contains child.target) && !(taskQueue contains child))
                taskQueue += child

            waitingTasks -= waitingTask
          }
        }

        if (taskQueue.nonEmpty) {
          // consider one task at a time
          val next = taskQueue.dequeue()

          if (next.parent.forall(x => actorMap contains x.target)) {
            val worker = context.actorOf(Props[Worker], name = next.target)
            worker ! InitWorker(next, next.parent.map(x => actorMap(x.target)))
            actorMap = actorMap + (next.target -> worker)

            // add dependencies to a queue only if not present yet
            for (child <- next.children)
              if (!(actorMap contains child.target) && !(taskQueue contains child))
                taskQueue += child
          } else {
            waitingTasks = waitingTasks :+ next
          }
        }
      }

    // message received when root task is built
    case CompletedDep() =>
      println("make completed")
      context.system.terminate()

    case ErrorDep() =>
      if(!exiting) {
        println("compilation error!")
        exiting = true
        context.system.terminate()
      }
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
      println("[" + self.path.name + "] initializing ...")
      curTask = task
      leftDep = task.children.size
      actorsToContact = toContact
      //println("[" + self.path.name + "] must contact " + actorsToContact.map(x => x.path.name))
      //println("[" + self.path.name + "] " + leftDep + " dependencies")
      if (leftDep <= 0)
        self ! MakeTask()

    // message received when all dependencies are satisfied, contacts parent after execution
    case MakeTask() =>
      if (!taskStarted) {
        taskStarted = true
        println("[" + self.path.name + "]")
        if (CommandRunner.run(curTask.command) == 0) {
          for (actor <- actorsToContact) {
            println("[" + self.path.name + "] task completed, contacting " + actor.path.name)
            actor ! CompletedDep()
          }
        } else {
          for (actor <- actorsToContact)
            actor ! ErrorDep()
        }

        context.stop(self)
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
  println(parser_makefile.root_task.target)
  */

  // start master actor
  val system = ActorSystem("distributed-make")
  val m = system.actorOf(Props[Master], name="master")
  m ! InitMake(parser_makefile)

}