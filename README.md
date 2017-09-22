# Article.ETA.Java.E3PAGateway

The EU’s Markets in Financial Instruments MIFID II Directive, will introduce significant changes for investment firms and other financial markets participants when the EU member states implemented them in January 2018. This article discusses the capability for existing TREP clients to transmit pre-trade FIX messages to registered Approved Publication Arrangements (APA) to satisfy MIFID II regulatory requirements.

The E3PA Gateway connects to APAs via the FIX protocol using the QuickFIX/J engine, logs all FIX traffic, and supports a simple management interface.

In many cases, data subject to pre-trade reporting obligations is already available via TREP in the form of MarketPrice updates which are shared via OMM posts. The E3PA Gateway will act as an additional endpoint for OMM post messages, and convert them to FIX format using a simple transformation directive and forward them to the configured APA for processing.

## Preqrequisites

### TREP Requirements
E3PA is compatible with TREP versions 2 and 3. Messages are delivered to it with on-stream or off-stream OMM Post messages.

### E3PA Requirements
E3PA requires any machine running Java, with a minimum of 2GB of memory and Internet access. Gradle is also required to build E3PA and pull down dependencies from Maven. Docker is required to build and run the Docker image if you chose that option.

## Building the project
Download the project which includes the E3PA Java source, build scripts and Thomson Reuters libraries on any machine running Java and Gradle.

In the directory containing the file `build.gradle`, run the command `gradle makeDist`. This will create a file named `apagateway_dist.tar.gz`. This file can then be moved to the location from where the E3PA gateway will be run.

## Building a Docker image
Also included is `Dockerfile` that can be used to create a Docker image. Run `docker build -t e3pa-gateway:latest .` from the directory with the Dockerfile after you have built the project with Gradle as outlined above. This will create a Docker image that can be run.

If you wish to run the image on a different machine, run `docker save -o e3pa-gateway.tar e3pa-gateway` to create a TAR file that can be moved. On the target machine run `docker load -i e3pa-gateway.tar` to create the image.

In the future you will also be able to download a Docker image from [DockerHub](https://hub.docker.com/).

## Project Directory Structure
* package/ -- root directory

  * app/ -- all software for the gateway   

  * client/omm/ -- FIX and UPA dictionaries

  * rules/ -- rules.json file location

  * gateway.sh -- script to start E3PA 

  * utils.sh -- script used by gateway.sh 


## Running the E3PA gateway server
E3PA can be run from a shell script or can be run from a Docker image. Both methods are describe below.

To start the E3PA server extract the `apagateway_dist.tar.gz` file created after you ran the Gradle build and go to the `package` directory. Run the `gateway.sh` with the `--server` option to start the server. There are many command line options available:


