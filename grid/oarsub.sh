#!/bin/bash

# https://www.grid5000.fr/w/Getting_Started#Deploying_your_nodes_to_get_root_access_and_create_your_own_experimental_environment

if [ $# -lt 2 ]
then
	echo 'Usage: $0 nodes walltime'
	echo 'walltime format: [hour:min:sec|hour:min|hour]'
	exit -1
fi

oarsub -I -l nodes=$1,walltime=$2 -t deploy