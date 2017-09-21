#!/bin/bash

export RULESHOME=../clienthome/rules

#
# We need to map a host directory for logs.
# Modify this for your environment
#
HOST_LOG_DIRECTORY=../clienthome/logs

#
# We need to map a host directory for logs.
# Modify thisn for your environment
#
HOST_RULES_FILE=../clienthome/rules/rules.json

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

#
# Dictionary path
#
DICTIONARY_PATH=../package/etc/APAGateway/omm/

./gateway.sh --fixaddress ${FIXIP} --fixport ${FIXPORT} --serviceid ${SERVICEID} --sender ${SENDER} --target ${TARGET} --verbose --service ${SERVICENAME} --rulesfile ${HOST_RULES_FILE}  --nofix --server  --service ${SERVICENAME} --server --fixdictionary ${DICTIONARY_PATH}/FDMFixFieldDictionary --fixenums ${DICTIONARY_PATH}/FDMenumtypes.def --postdictionary ${DICTIONARY_PATH}/RDMFieldDictionary --postenums ${DICTIONARY_PATH}/enumtype.def

