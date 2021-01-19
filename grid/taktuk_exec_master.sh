cd ./systeme-distribues/dist_make
#command="runMain sample.cluster.simple.App "$1" 25251"
#sbt $command
echo "ready"
RC=1
echo "$RC"
while [ $RC -ne 137 -a $RC -ne 0 ]
do 
	echo "run sbt master"
	sbt "runMain fr.ensimag.sysd.distr_make.App master $1"
	RC=$?
	echo "exit master with code $RC"
done
