#!/bin/bash

if [[ $# -ne 2 ]]
then
  echo "Argument not correct, put number of workers and port"
  exit
fi

PWD="`(cd $(dirname \"$0\") && pwd )`"
TAKTUK="${PWD}/taktuk.sh"
TAKTUK_EXEC="${PWD}/taktuk_exec.sh"
TAKTUK_EXEC_MASTER="${PWD}/taktuk_exec_master.sh"
# PLAYBOOK="${PWD}/provision.yml"

kadeploy3 -f $OAR_NODE_FILE -e debian10-x64-nfs -k

# connect to reserved nodes as root and with self-propagation
taktuk -l root -s -o connector -o status -o output='"$host: $line\n"' -f <( uniq $OAR_FILE_NODES ) broadcast exec [ $TAKTUK ]

cd ../dist_make
python create_conf.py $1 $2

cd ../grid
taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | tail -n +2 | head -$1 ) broadcast exec [ $TAKTUK_EXEC $2 ] &
taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( cat nodes.txt | head -1 ) broadcast exec [ $TAKTUK_EXEC_MASTER $2 ]
