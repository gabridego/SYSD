cd ./systeme-distribues/akka-sample-cluster-scala
#command="runMain sample.cluster.simple.App "$1" 25251"
#sbt $command 
sbt "runMain sample.cluster.simple.App 25251"