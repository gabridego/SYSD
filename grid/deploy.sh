#!/bin/bash

PWD="`(cd $(dirname \"$0\") && pwd )`"
TAKTUK="${PWD}/taktuk.sh"
# PLAYBOOK="${PWD}/provision.yml"

kadeploy3 -f $OAR_NODE_FILE -e debian10-x64-nfs -k

# connect to reserved nodes as root and with self-propagation
taktuk -l root -s -o connector -o status -o output='"$host: $line\n"' -f <( uniq $OAR_FILE_NODES ) broadcast exec [ $TAKTUK ]
