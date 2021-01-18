cd ./systeme-distribues/dist_make
echo "ready"
RC=1
echo "$RC"
while [ $RC -ne 137 -a $RC -ne 0  ]
do
	echo "run sbt worker"
	sbt "runMain fr.ensimag.sysd.distr_make.App backend $1"
	RC=$?
	echo "exit worker with code $RC"
done
