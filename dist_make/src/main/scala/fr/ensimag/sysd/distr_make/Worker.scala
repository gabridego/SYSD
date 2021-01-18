package fr.ensimag.sysd.distr_make


import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors
import fr.ensimag.sysd.CborSerializable

//#worker
object Worker {

  val WorkerServiceKey = ServiceKey[Worker.MakeTask]("Worker")

  sealed trait Command
  final case class MakeTask(task: String, replyTo: ActorRef[TaskCompleted]) extends Command with CborSerializable
  final case class TaskCompleted(text: String) extends CborSerializable

  def apply(): Behavior[Command] =
    Behaviors.setup { ctx =>
      // each worker registers themselves with the receptionist
      ctx.log.info("Registering myself with receptionist")
      ctx.system.receptionist ! Receptionist.Register(WorkerServiceKey, ctx.self)

      Behaviors.receiveMessage {
        case MakeTask(task, replyTo) =>

          CommandRunner.run(task)
          replyTo ! TaskCompleted(task)
          Behaviors.same
      }
    }
}
//#worker
