package fr.ensimag.sysd.distr_make

import scala.language.postfixOps
import scala.sys.process._
//import sys.process._

object CommandRunner {
  def run(command : String): Int={

    //"ls -al" #| "grep Foo" ! //"ls -al | grep Foo" !
    //"premier 2 `echo 1*200000000/20-1 |bc` > list1.txt" !
    //Seq("pwd") .!
    //Seq("/bin/sh", "premier", "2 `echo 1*200000000/20-1 |bc` > list1.txt") .!
    //Process("premier 2 `echo 1*200000000/20-1 |bc` > list1.txt") !
    //var cmd = "gcc premier.c -o premier -lm" // Your command
    //var output = cmd.!! // Captures the output
    //cmd = "premier 2 `echo 1*200000000/20-1 |bc` > list1.txt"
    //output = cmd.!! // Captures the output
    //val result = "ls -al" !
    //println(result);
    //val cmd = "bash -lic  \"premier 2 `echo 1*200000000/20-1 |bc` > list1.txt\""

    println("\t" + command)
    val cmd = "bash -c  \"" + command + "\"" //-c when you want to execute a command specifically with that shell instead of bash
    val output = cmd.!
    return output
  }
}
