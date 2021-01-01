#!/bin/bash

echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
curl -sl "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
apt update
# apt --yes install ansible
apt --yes install sbt scala
# do we really need ansible?
# ansible-playbook $1
