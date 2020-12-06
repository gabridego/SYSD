package fr.ensimag.sysd.distr_make
import java.nio.file.{Files, Paths}
import scala.language.implicitConversions
import scala.collection.mutable
import akka.actor.Props
import akka.actor.ActorSystem
import akka.actor.Actor
import scala.sys.process._

object Master extends App {
  /*
  var def_makefile = ""
  val listMake=List("GNU-makefile", "makefile", "Makefile")
  for(makefile <- listMake){
    if (Files.exists(Paths.get(makefile)) ){
      def_makefile = makefile
    }}*/

  /* Argument Parser */
  if(args.length != 3)
    {
      throw new Exception("No found arguments")
    }
  var make = args(0).toString()
  var target = args(2).toString()
  println(make)
  val makefile_parser = new Parser(make)
  val task = makefile_parser.get_task(target)
  val deepTree = new DeepTree(task,makefile_parser)

  // Run Task Worker
  //for(t <- deepTree.leaves){

  //}
  // Run Async args(1).toString()



}
class DeepTree(task: Task, parser : Parser) {
  if(task.is_file_dependency())
    throw new Exception("Cannot build a tree from a file dependency")
  var nodes_num = 0
  var node: Task = _
  var t: Task = _
  var depend: List[String] =  List[String]()
  var leaves: mutable.Set[Task] = mutable.Set[Task]()
  val stack = mutable.Stack(task)
  while (stack.nonEmpty) {
    node = stack.pop()
    nodes_num += 1
    for (n <- node.dependencies) {
      if (parser.get_task(n).is_file_dependency()) { //is_file_dependency()
        depend ::= n
      }

    }
    node.dependencies = depend map (x => x )


    if (depend.isEmpty) {
      leaves.add(node)
    }
    else {
      for (n <- node.dependencies) {
        t = parser.get_task(n)
        t.children = node :: t.children
        stack.push(t)
      }

    }
    depend = null
  }
}














