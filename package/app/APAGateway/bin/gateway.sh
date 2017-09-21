#!/bin/sh

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


. /app/APAGateway/bin/utils.sh

if [[ "${mode}" = "client" ]] ; then
    cp /app/APAGateway/config/client_logback.xml /app/APAGateway/config/logback.xml
    run_application ${mode} com.thomsonreuters.atr.gateway.apa.APAConsumer $@
elif [[ "${mode}" = "server" ]] ; then
    cp /app/APAGateway/config/server_logback.xml /app/APAGateway/config/logback.xml
    run_application ${mode} com.thomsonreuters.atr.gateway.apa.APAProducer $@
elif [[ "${mode}" = "simulator" ]] ; then
    cp /app/APAGateway/config/simulator_logback.xml /app/APAGateway/config/logback.xml
    run_application ${mode} com.thomsonreuters.atr.gateway.fix.FixSimulator $@
else
    echo "Specify either --client or --server or --simulator"

fi