package fr.ensimag.sysd.distr_make

import java.io._

object TimerSingleton {
  
    //STOPWATCHES
    var stopwatchActorCreation: Long = 0
    var stopwatchParsingTreeCreation: Long = 0
    var stopwatchActorExecution: Long = 0
    var stopwatchActorCreationAndExecution: Long = 0
    var stopwatchActorCreationAndParsingTreeCreationAndExecution: Long = 0
    var alreadyStopped : Boolean = false
  
    def startCreationActor(): Unit = {
        //COMPUTATION STOPWATCHES
        println("STARTING stopwatchActorCreation")
        stopwatchActorCreation = System.currentTimeMillis()
    }

    def startCreationParsingTreeAndStopActorCreation(): Unit = {
        //COMPUTATION STOPWATCHES
        println("STARTING stopwatchParsingTreeCreation AND STOPPING stopwatchActorCreation")
        stopwatchParsingTreeCreation = System.currentTimeMillis()
        stopwatchActorCreation = stopwatchParsingTreeCreation - stopwatchActorCreation
        println("TIME SPENT IN ACTOR CREATION: " + stopwatchActorCreation + "ms")
        writeFile("TIME SPENT IN ACTOR CREATION: " + stopwatchActorCreation + "ms\n")
    }

    def startActorExecutionAndStopParsingTreeCreation(): Unit = {
        //COMPUTATION STOPWATCHES
        println("STARTING stopwatchActorExecution AND STOPPING stopwatchParsingTreeCreation")
        stopwatchActorExecution = System.currentTimeMillis()
        stopwatchParsingTreeCreation = stopwatchActorExecution - stopwatchParsingTreeCreation
    }

    def stopTimersExecutionComplete(): Unit = {

        if(!alreadyStopped) {

            alreadyStopped = true

            //COMPUTATION STOPWATCHES
            stopwatchActorExecution = System.currentTimeMillis() - stopwatchActorExecution
            stopwatchActorCreationAndExecution = stopwatchActorCreation + stopwatchActorExecution
            stopwatchActorCreationAndParsingTreeCreationAndExecution = stopwatchActorCreation + stopwatchParsingTreeCreation + stopwatchActorExecution
            println("TIME SPENT IN ACTOR CREATION MASTER: " + stopwatchActorCreation + "ms")
            println("TIME SPENT IN PARSING TREE CREATION: " + stopwatchParsingTreeCreation + "ms")
            println("TIME SPENT IN EXECUTION: " + stopwatchActorExecution + "ms")
            println("TIME SPENT IN ACTOR CREATION + EXECUTION: " + stopwatchActorCreationAndExecution + "ms")
            println("TIME SPENT IN ACTOR CREATION + PARSING TREE CREATION + EXECUTION: " + stopwatchActorCreationAndParsingTreeCreationAndExecution + "ms")
            writeFile("TIME SPENT IN ACTOR CREATION MASTER: " + stopwatchActorCreation + "ms\n" + "TIME SPENT IN PARSING TREE CREATION: " + stopwatchParsingTreeCreation + "ms\n" + "TIME SPENT IN EXECUTION: " + stopwatchActorExecution + "ms\n" + "TIME SPENT IN ACTOR CREATION + EXECUTION: " + stopwatchActorCreationAndExecution + "ms\n" + "TIME SPENT IN ACTOR CREATION + PARSING TREE CREATION + EXECUTION: " + stopwatchActorCreationAndParsingTreeCreationAndExecution + "ms\n")
        }
    }

    def writeFile(message: String): Unit = {
        val fw = new FileWriter("metrics.txt", true)
        try {
        fw.write(message)
        }
        finally fw.close() 
    }

}