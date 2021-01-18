cd ./systeme-distribues/dist_make
#command="runMain sample.cluster.simple.App "$1" 25251"
#sbt $command 
sbt "runMain fr.ensimag.sysd.distr_make.App frontend $1"
