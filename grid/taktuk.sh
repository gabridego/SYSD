#!/bin/bash

apt update
apt --yes install ansible

ansible-playbook $1