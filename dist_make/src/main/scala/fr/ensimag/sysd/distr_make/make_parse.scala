package fr.ensimag.sysd.distr_make

import scala.collection.mutable.Stack
import scala.io.Source
import java.nio.file.{Paths, Files}

/** Basic Task class
 *
 *  @param target         name of the rule
 *  @param dependencies   list of names of dependencies
 *  @param command        command to execute
 */
class Task(val target: String, var dependencies: List[Task], var command: String) {
  var parent: List[Task] = List[Task]()     // list of targets that depend on the current task
  var children: List[Task] = List[Task]()   // list of targets the current task depends on
  def is_file(): Boolean={
    if(dependencies.isEmpty && command == "")
      return true
    return false
  }
}

/** Parse specified Makefile
 *
 *  @param filename       path to Makefile
 */
class Parser(val filename: String) {
  var root_task:Task = _
  var tasks = Map[String, Task]()

  /** Reads Makefile, create tasks and build dependencies
   *
   *  @return             list of parsed tasks
   */
  def create_list_task(): Unit={
    var index = 0
    val bufferedSource = Source.fromFile(filename)
    val tab = bufferedSource.getLines.filter(_ != "").filter(_(0) != '#').toArray

    while (index < tab.length){
      val current_line = tab(index)
      if (!current_line.contains(':')){
        throw new Exception("Missing : separator on line:" + current_line)
      }
      if (current_line(0) == '\t'){
        throw new Exception("Expected target, found command on line " + current_line)
      }
      val current_recipe = current_line.split(':')
      val current_target = current_recipe(0).trim

      if (current_target == ""){
        throw new Exception("No target specified on line " + current_line)
      }

      var dependencies = ""
      var depList = List[Task]()
      if (current_recipe.length == 2) {
        dependencies = current_recipe(1).trim
        depList = dependencies.split(' ').map(t => get_task(t)).toList
      }

      index += 1
      var cmd = ""
      if (tab(index)(0) == '\t'){
        cmd = tab(index).trim
        index += 1
      }

      val current_task = get_task(current_target)
      current_task.dependencies = depList
      current_task.command = cmd

      // first rule is the main task
      if (index == 1 || (index == 2 && cmd != ""))
        root_task = current_task
    }
    bufferedSource.close
  }

  create_list_task()

  def print_all_target(): Unit ={
    for (target <- tasks.keys) {
      println("Target: " + tasks(target).target)
      println("Dep: " + tasks(target).dependencies)
    }
  }

  /** Returns a task given its name. Creates one if not exists
   *
   *  @param target
   *  @return
   */
  def get_task(target : String): Task ={
    /*try
      return tasks(target)
    catch {
      case e: NoSuchElementException => throw new NoSuchElementException("No task for this target :" + target)
    }
    */
    if (tasks contains target)
      return tasks(target)
    else {
      var task = new Task(target, List[Task](), "")
      tasks = tasks + (target -> task)
      return task
    }
  }

  /** Build dependencies graph from a root target, assigning parents and children to involved tasks
   *
   *  @param target
   */
  def create_graph(target: String): Unit = {
    if(target != "")
      root_task = get_task(target)

    var depend: List[Task] =  List[Task]()
    val stack: Stack[Task] = Stack(root_task)

    while (stack.nonEmpty) {
      val node = stack.pop()

      if (node.dependencies.nonEmpty) {
        for (n <- node.dependencies)
          if (!n.is_file())
            depend ::= n
          else
            if (!Files.exists(Paths.get(n.target)))
              throw new Exception("File " + n.target + " not found")
        node.dependencies = depend map (x => x)

        if (depend.nonEmpty) {
          for (n <- node.dependencies) {
            node.children = node.children :+ n
            n.parent = n.parent :+ node
            stack.push(n)
          }
        }
      }

      depend = List[Task]()
    }

    /*
    for (task <- tasks){
      if (task.dependencies.head != "")
        for (dep <- task.dependencies){
          val current_parent = get_task(dep)
          task.parent =  task.parent :+ current_parent
          current_parent.children = task :: current_parent.children
        }
     */


  }

}
