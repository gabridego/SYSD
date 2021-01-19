#!/bin/bash

if [[ $# -ne 2 ]]
then
  echo "Argument not correct, put number of workers and port"
  exit
fi

PWD="`(cd $(dirname \"$0\") && pwd )`"
TAKTUK="${PWD}/taktuk.sh"
# PLAYBOOK="${PWD}/provision.yml"

kadeploy3 -f $OAR_NODE_FILE -e debian10-x64-nfs -k

# connect to reserved nodes as root and with self-propagation
taktuk -l root -s -o connector -o status -o output='"$host: $line\n"' -f <( uniq $OAR_FILE_NODES ) broadcast exec [ $TAKTUK ]

cd ../dist_make
python create_conf.py $2 $1

cd ../grid
./run_make.sh $1 $2
