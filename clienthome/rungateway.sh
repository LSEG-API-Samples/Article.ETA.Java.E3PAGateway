#!/bin/bash


#
# Docker image tag
#
DOCKER_IMAGE=apagateway/latest


#
# We need to map a host directory for logs.
# Modify this for your environment
#
HOST_LOG_DIRECTORY=/Users/egoebelbecker/ideaProjects/APAGateway-new/clienthome/logs

#
# We need to map a host directory for logs.
# Modify thisn for your environment
#
HOST_RULES_DIRECTORY=/Users/egoebelbecker/ideaProjects/APAGateway-new/clienthome/rules

#
# TREP Service name
#
SERVICENAME=AUTEX_APA

#
# TREP Service ID
#
SERVICEID=74


#
# FIX sender
#
SENDER=APAGATEWAY

#
# FIX target
#
TARGET=AUTEX

#
# FIX IP Address
#
FIXIP=192.168.1.1

#
# FIX port
#
FIXPORT=8001

docker run --net=host -p 14003:14003 -v ${HOST_LOG_DIRECTORY}:/var/log/APAGateway -v ${HOST_RULES_DIRECTORY}:/etc/APAGateway/rules -t ${DOCKER_IMAGE} --fixaddress ${FIXIP} --fixport ${FIXPORT} --serviceid ${SERVICEID} --sender ${SENDER} --target ${TARGET} --verbose --service ${SERVICENAME} --server

