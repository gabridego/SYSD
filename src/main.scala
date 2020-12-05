import scala.io.Source
import scala.collection.mutable.ListBuffer


class Task(val target: String, val dependencies: List[String], val command: String){
    var parent = List[Task]()
    var children = List[Task]()
}

class Parser(val filename: String){

    def create_list_task(): List[Task]={
        var buffer_tasks = new ListBuffer[Task]()
        var index = 0
        val bufferedSource = Source.fromFile(filename)
        val tab = bufferedSource.getLines.filter(_ != "").toArray
        while (index < tab.size){
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
            if (current_recipe.size == 2){
              dependencies = current_recipe(1).trim
            }
            index += 1
            val cmd = tab(index)
            if (cmd(0) != '\t'){
                throw new Exception("No tabulation detected" + tab(index))
            }
            index += 1
            var current_task = new Task(current_target, dependencies.split(' ').toList, cmd.trim)
            buffer_tasks += current_task
        }
        bufferedSource.close
        return buffer_tasks.toList
    }

    val tasks = create_list_task()

    def print_all_target(): Unit ={
        for (task <- tasks) println(task.target)
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
            if (task.dependencies(0) != ""){
                for (dependencie <- task.dependencies){
                    val current_parent = get_task(dependencie)
                    task.parent =  current_parent :: task.parent
                    current_parent.children = task :: current_parent.children
                }
            }

        }
    }

}

object parser{
    def main(args: Array[String]): Unit = {
        val filename = "Makefile"
        val parser_makefile = new Parser(filename)
        parser_makefile.print_all_target()
        parser_makefile.create_graph()
    }

}
