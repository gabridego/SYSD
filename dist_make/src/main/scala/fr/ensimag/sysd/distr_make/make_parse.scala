package fr.ensimag.sysd.distr_make

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Stack
import scala.io.Source

/** Basic Task class
 *
 *  @param target         name of the rule
 *  @param dependencies   list of names of dependencies
 *  @param command        command to execute
 */
class Task(val target: String, var dependencies: List[String], val command: String) {
  var parent = List[Task]()     // list of targets that depend on the current task
  var children = List[Task]()   // list of targets the current task depends on
  def is_file_dependency(): Boolean={
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

  /** Reads Makefile, create tasks and build dependencies
   *
   *  @return             list of parsed tasks
   */
  def create_list_task(): List[Task]={
    var buffer_tasks = new ListBuffer[Task]()
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
      var depList = List[String]()
      if (current_recipe.length == 2) {
        dependencies = current_recipe(1).trim
        depList = dependencies.split(' ').toList
      }

      index += 1
      var cmd = ""
      if (tab(index)(0) == '\t'){
        cmd = tab(index).trim
        index += 1
      }

      var current_task = new Task(current_target, depList, cmd)

      // first rule is the main task
      if (index == 1 || (index == 2 && cmd != ""))
        root_task = current_task

      buffer_tasks += current_task
    }
    bufferedSource.close
    return buffer_tasks.toList
  }

  val tasks = create_list_task()

  def print_all_target(): Unit ={
    for (task <- tasks) {
      println("Nome Target: " + task.target)
      println("Dep: " + task.dependencies)
    }
  }

  /** Returns a task given its name
   *
   *  @param target
   *  @return
   */
  def get_task(target : String): Task ={
    for (task <- tasks) {
      if (task.target == target){
        return task
      }
    }
    throw new Exception("No task for this target :" + target)

  }

  /** Build dependencies graph from a root target, assigning parents and children to involved tasks
   *
   *  @param target
   */
  def create_graph(target: String): Unit = {
    if(target != "")
      root_task = get_task(target)

    var depend: List[String] =  List[String]()
    val stack = Stack(root_task)

    while (stack.nonEmpty) {
      val node = stack.pop()

      if (node.dependencies.size > 0) {
        for (n <- node.dependencies)
          if (!(get_task(n).is_file_dependency()))
            depend ::= n
        node.dependencies = depend map (x => x)

        if (!(depend.isEmpty)) {
          for (n <- node.dependencies) {
            val t = get_task(n)
            node.children = node.children :+ t
            t.parent = t.parent :+ node
            stack.push(t)
          }
        }
      }

      depend = List[String]()
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
