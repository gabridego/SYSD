package fr.ensimag.sysd.distr_make

import scala.collection.mutable.{ListBuffer, Queue}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.rogach.scallop._


import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import fr.ensimag.sysd.CborSerializable

//#worker
object Worker {

  val WorkerServiceKey = ServiceKey[Worker.TransformText]("Worker")
  var curTask: Task = _
  var leftDep: Int = _                      // remaining dependencies to satisfy
  var taskStarted: Boolean = false          // flag to ensure command is run only once
  var actorsToContact: List[akka.actor.ActorRef] = _   // list of parent workers

  sealed trait Command
  final case class TransformText(text: String, replyTo: akka.actor.typed.ActorRef[TextTransformed]) extends Command with CborSerializable
  final case class TextTransformed(text: String) extends CborSerializable

  def apply(): Behavior[Command] =
    Behaviors.setup { ctx =>
      // each worker registers themselves with the receptionist
      ctx.log.info("Registering myself with receptionist")
      ctx.system.receptionist ! Receptionist.Register(WorkerServiceKey, ctx.self)

      akka.actor.typed.scaladsl.Behaviors.receiveMessage {
        case TransformText(text, replyTo) =>
          replyTo ! TextTransformed(text.toUpperCase)


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
     Behaviors.same
      }
    }
}
//#worker
