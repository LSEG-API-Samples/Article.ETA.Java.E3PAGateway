#! /bin/sh

#
# Assuming that the jars is ../libs are what is needed
#

#
# Since the old version was too simple, we need a different version for mac now
#
run_application () {

    export LIBS=/app/APAGateway/libs
    export RULESHOME=/etc/APAGateway/rules
    CLASSPATH=${CLASSPATH}:${LIBS}/*:${LIBS}
    export LOG4JCONFIG="-Dlogback.configurationFile=/app/APAGateway/config/logback.xml"
    echo $CLASSPATH
    APP=$2
    mode=$1
    AWS=0
    ## use dmicode to determine the machine is AWS machine or not
    ##DMICODE=`dmidecode --string system-uuid` //not every machine support this command
    DMICODE=`cat /sys/class/dmi/id/product_uuid` ##this needs sudo permission, this script is for docker, so it is ok
    if [[ ${DMICODE} == "EC"* ]]; then
        AWS=1
    fi
    ## get public ip in different ways:  in AWS machine use AWS metadata; in other machines use unix command
    if [[ ${AWS} == "1" ]]; then
        IP=`curl http://169.254.169.254/latest/meta-data/public-ipv4`
    elif [[ "$(uname)" != "Darwin" ]] ; then
        IP=$(hostname -I | cut -d " " -f 1)
    fi

    if [[ "$(uname)" = "Darwin" ]] ; then
        java -enableassertions -cp ${CLASSPATH} -Dlog4j.debug $APP $@
    elif [[ "${mode}" = "server" ]] ; then
        ${JAVA_HOME}/bin/java -enableassertions -cp ${CLASSPATH} ${LOG4JCONFIG} -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.rmi.port=6998 -Dcom.sun.management.jmxremote.port=6998 -Djava.rmi.server.hostname=${IP} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.local.only=false ${APP} $@
    else
        ${JAVA_HOME}/bin/java -enableassertions -cp ${CLASSPATH} ${LOG4JCONFIG} $APP $@
    fi
}



