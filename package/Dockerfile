FROM centos

ENV JAVA_VERSION 8u31

RUN yum --assumeyes install java-1.8.0-openjdk
RUN yum --assumeyes update
RUN yum --assumeyes install dos2unix

# Set the working directory to /app
WORKDIR /app

ENV PACKAGE_ROOT /app

# Build home for app
ADD app/ /app/

ADD etc/ /etc/

RUN dos2unix APAGateway/bin/gateway.sh
RUN dos2unix APAGateway/bin/utils.sh

ENTRYPOINT ["APAGateway/bin/gateway.sh"]
CMD []

