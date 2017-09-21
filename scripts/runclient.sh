#!/bin/bash


#
# TREP Service name
#
SERVICENAME=AUTEX_APA


#
# Dictionary location
#
DICTIONARY_PATH=../package/etc/APAGateway/omm/

./gateway.sh -p 14003 -s ${SERVICENAME} -u rmds -h 127.0.0.1 --client --fixdictionary ${DICTIONARY_PATH}/RDMFieldDictionary --fixenums ${DICTIONARY_PATH}/enumtype.def $@
