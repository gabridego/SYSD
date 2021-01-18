package fr.ensimag.sysd.distr_make

import scala.collection.mutable.{ListBuffer, Queue, HashSet}
import scala.concurrent.duration._
import akka.util.Timeout
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

import scala.util.Failure
import scala.util.Success

//#frontend
object Frontend {

  sealed trait Event
  private case object Tick extends Event
  private final case class WorkersUpdated(newWorkers: Set[ActorRef[Worker.MakeTask]]) extends Event
  private final case class TransformCompleted(originalText: String, transformedText: String) extends Event
  private final case class JobFailed(why: String, task: Task) extends Event


  def apply(): Behavior[Event] = Behaviors.setup { ctx =>
    Behaviors.withTimers { timers =>
      // subscribe to available workers
      val subscriptionAdapter = ctx.messageAdapter[Receptionist.Listing] {
        case Worker.WorkerServiceKey.Listing(workers) =>
          WorkersUpdated(workers)

      }
      ctx.system.receptionist ! Receptionist.Subscribe(Worker.WorkerServiceKey, subscriptionAdapter)
      val filename = "Makefile"
      var target = ""
      val parser_makefile = new Parser(filename)
      parser_makefile.create_graph(target)
      var taskQueue = Queue[Task]()
      var waitingTasks = HashSet[Task]()
      var taskDone = HashSet[String]()
      //ctx.log.info(parser_makefile.root_task.children(0).command)
      //ctx.log.info(taskDone(parser_makefile.root_task.command).toString())

      // to start processing, add root task to task to process
      ctx.log.info("adding {} to queue", parser_makefile.root_task.target)
      taskQueue += parser_makefile.root_task
      ctx.log.info("tasks in queue: {}", taskQueue.map(x => x.target))

      timers.startTimerWithFixedDelay(Tick, Tick, 2.seconds)

      running(ctx, IndexedSeq.empty, jobCounter = 0, taskQueue, waitingTasks, taskDone)
    }
  }

  private def running(ctx: ActorContext[Event], workers: IndexedSeq[ActorRef[Worker.MakeTask]], jobCounter: Int, taskQueue: Queue[Task], waitingTasks: HashSet[Task], taskDone: HashSet[String]): Behavior[Event] =
    Behaviors.receiveMessage {
      case WorkersUpdated(newWorkers) =>
        ctx.log.info("List of services registered with the receptionist changed: {}", newWorkers)
        running(ctx, newWorkers.toIndexedSeq, jobCounter, taskQueue, waitingTasks, taskDone)

      case Tick =>
        if (waitingTasks.isEmpty && taskQueue.isEmpty){
          ctx.log.info("empty lists!!!")
          for (worker <- workers) {
            implicit val timeout: Timeout = 600.seconds
            ctx.ask(worker, Worker.MakeTask("", _)) {
              case Success(transformedText) => TransformCompleted("","")
              case Failure(ex) => JobFailed("", new Task("", List[Task](), ""))
            }
          }
          CommandRunner.run("kill -9 $(pidof java)")
          
        }

        val currentTask = choiceTask(taskQueue, waitingTasks, taskDone)
        ctx.log.info("current task: {}", currentTask.target)
        ctx.log.info("tasks in queue: {}", taskQueue.map(x => x.target))
        ctx.log.info("waiting tasks: {}", waitingTasks.map(x => x.target))
        if (workers.isEmpty) {
          ctx.log.warn("Got tick request but no workers available, not sending any work")
          taskQueue += currentTask
          Behaviors.same
        } else if (currentTask.command == ""){
          ctx.log.info("Waiting for work")
          Behaviors.same
        } else {
          // how much time can pass before we consider a request failed
          implicit val timeout: Timeout = 600.seconds
          val selectedWorker = workers(jobCounter % workers.size)
          ctx.log.info("Sending task {} for processing to {}", currentTask.target, selectedWorker)
          ctx.ask(selectedWorker, Worker.MakeTask(currentTask.command, _)) {
            case Success(transformedText) => TransformCompleted(transformedText.text, transformedText.text)
            case Failure(ex) => JobFailed("Processing timed out", currentTask)
          }
          running(ctx, workers, jobCounter + 1, taskQueue, waitingTasks, taskDone)
        }

      case TransformCompleted(originalText, transformedText) =>
        taskDone += transformedText
        ctx.log.info("Got completed run of: {}", transformedText)
        Behaviors.same

      case JobFailed(why, currentTask) =>
        ctx.log.warn("Run of {} failed. Because: {}", currentTask.command, why)
        taskQueue += currentTask
        Behaviors.same

    }

    def choiceTask(taskQueue: Queue[Task], waitingTasks: HashSet[Task], taskDone: HashSet[String]): Task =
      if (waitingTasks.isEmpty && taskQueue.isEmpty){
        return new Task("", List[Task](), "")
        //program should end
      } else {
        while (!taskQueue.isEmpty){
          val task = taskQueue.dequeue()
          for (child <- task.children){
            if (!taskQueue.contains(child) && !taskDone(child.command))
              taskQueue += child
          }
          if (!taskDone(task.command) && task.children.forall(x => taskDone(x.command))){
            return task
          } else {
            waitingTasks += task
          }
        }
        for (waitingtask <- waitingTasks){
          if (!taskDone(waitingtask.command) && waitingtask.children.forall(x => taskDone(x.command))){
            waitingTasks -= waitingtask
            return waitingtask
          }
        }
        return new Task("", List[Task](), "")
      }

}
