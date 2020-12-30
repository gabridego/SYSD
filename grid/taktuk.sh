#!/bin/bash

apt update
apt --yes install ansible
ansible-playbook -i inventory.ini provision.yml