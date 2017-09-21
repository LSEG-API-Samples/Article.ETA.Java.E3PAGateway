#!/bin/bash

#
# Docker image tag
#
DOCKER_IMAGE=apagateway/latest

#
# We need to map a host directory for logs.
# Modify this for your environment
#
HOST_LOG_DIRECTORY=/Users/egoebelbecker/ideaProjects/APAGateway-new/clienthome/clientlogs

#
# We need to map a host directory for test post messages
# Modify this for your environment
#
HOST_POST_DIRECTORY=/Users/egoebelbecker/ideaProjects/APAGateway-new/clienthome

#
# TREP Service name
#
SERVICENAME=AUTEX_APA


docker run -i --net=host -v ${HOST_LOG_DIRECTORY}:/var/log/APAConsumer -v ${HOST_POST_DIRECTORY}:/etc/apagateway/posts -t ${DOCKER_IMAGE} -p 14003 -s ${SERVICENAME} -u rmds -h 127.0.0.1 --client $@
