#!/bin/bash

mode="unset"

for i in $@
do
    if [[ "${i}" = "--client" ]] ; then
        mode="client"
    fi
    if [[ "${i}" = "--server" ]] ; then
        mode="server"
    fi
    if [[ "${i}" = "--simulator" ]] ; then
        mode="simulator"
    fi
done


. ./utils.sh

if [[ "${mode}" = "client" ]] ; then
    run_application ${mode} com.thomsonreuters.atr.gateway.apa.APAConsumer $@
elif [[ "${mode}" = "server" ]] ; then
    run_application ${mode} com.thomsonreuters.atr.gateway.apa.APAProducer $@
elif [[ "${mode}" = "simulator" ]] ; then
    run_application ${mode} com.thomsonreuters.atr.gateway.fix.FixSimulator $@
else
    echo "Specify either --client or --server or --simulator"

fi