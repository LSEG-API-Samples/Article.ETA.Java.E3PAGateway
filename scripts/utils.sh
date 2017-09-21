#! /bin/sh

#
# Assuming that the jars is ../libs are what is needed
#

#
# Since the old version was too simple, we need a different version for mac now
#
run_application () {

    export LIBS=../package/app/APAGateway/libs
    CLASSPATH=${CLASSPATH}:${LIBS}/*:${LIBS}:./slf4j/*
    export LOG4JCONFIG="-Dlogback.configurationFile=/app/APAGateway/libs/logback.xml"
    echo $CLASSPATH
    APP=$2
    mode=$1


    if [[ "$(uname)" = "Darwin" ]] ; then
        java -enableassertions -cp ${CLASSPATH} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.rmi.port=6998 -Dcom.sun.management.jmxremote.port=6998 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false -Dlog4j.debug $APP $@
    elif [[ "${mode}" = "server" ]] ; then
        ${JAVA_HOME}/bin/java -enableassertions -cp ${CLASSPATH} ${LOG4JCONFIG} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.rmi.port=6998 -Dcom.sun.management.jmxremote.port=6998 -Djava.rmi.server.hostname=${IP} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false ${APP} $@
    else
        ${JAVA_HOME}/bin/java -enableassertions -cp ${CLASSPATH} ${LOG4JCONFIG} $APP $@
    fi
}



