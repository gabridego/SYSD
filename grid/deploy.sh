#!/bin/bash

PWD="`(cd $(dirname \"$0\") && pwd )`"
TAKTUK="${PWD}/taktuk.sh"
TAKTUK_EXEC="${PWD}/taktuk_exec.sh"
# PLAYBOOK="${PWD}/provision.yml"

kadeploy3 -f $OAR_NODE_FILE -e debian10-x64-nfs -k

# connect to reserved nodes as root and with self-propagation
taktuk -l root -s -o connector -o status -o output='"$host: $line\n"' -f <( uniq $OAR_FILE_NODES ) broadcast exec [ $TAKTUK ]

cd ../akka-sample-cluster-scala
python create_conf.py
cd ../dist_make
python create_conf.py

taktuk -s -o connector -o status -o output='"$host: $line\n"' -f <( uniq $OAR_FILE_NODES ) broadcast exec [ $TAKTUK_EXEC ]
