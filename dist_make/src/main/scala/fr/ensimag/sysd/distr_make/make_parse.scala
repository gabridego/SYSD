package fr.ensimag.sysd
package distr_make

import scala.collection.mutable.ListBuffer
import scala.io.Source

class Task(val target: String, var dependencies: List[String], val command: String) {
  var parent = List[Task]()
  var children = List[Task]()
  def is_file_dependency(): Boolean={
    if(dependencies.isEmpty && command == ""){
      return true
    }
    return false
  }

}

class Parser(val filename: String) {
  var root_task:Task = _

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
      if (current_recipe.length == 2){
        dependencies = current_recipe(1).trim
      }
      index += 1
      var cmd = ""
      if (tab(index)(0) == '\t'){
        cmd = tab(index).trim
        index += 1
      }
      var current_task = new Task(current_target, dependencies.split(' ').toList, cmd)

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
      print("Nome Target")
      println(task.target)
      println("Dep:"+task.dependencies)
      println(" ")}

  }

  def get_task(target : String): Task ={
    for (task <- tasks) {
      if (task.target == target){
        return task
      }
    }
    throw new Exception("No task for this target :" + target)

  }

  def create_graph(): Unit ={
    for (task <- tasks){
      if (task.dependencies.head != "")
        for (dep <- task.dependencies){
          val current_parent = get_task(dep)
          task.parent =  task.parent :+ current_parent
          current_parent.children = task :: current_parent.children
        }

    }
  }

}
