# Article.ETA.Java.E3PAGateway

The EU’s Markets in Financial Instruments MIFID II Directive, will introduce significant changes for investment firms and other financial markets participants when the EU member states implemented them in January 2018. This article discusses the capability for existing TREP clients to transmit pre-trade FIX messages to registered Approved Publication Arrangements (APA) to satisfy MIFID II regulatory requirements.

The E3PA Gateway connects to APAs via the FIX protocol using the QuickFIX/J engine, logs all FIX traffic, and supports a simple management interface.

In many cases, data subject to pre-trade reporting obligations is already available via TREP in the form of MarketPrice updates which are shared via OMM posts. The E3PA Gateway will act as an additional endpoint for OMM post messages, and convert them to FIX format using a simple transformation directive and forward them to the configured APA for processing.

# TREP Requirements
E3PA is compatible with TREP versions 2 and 3. Messages are delivered to it with on-stream or off-stream OMM Post messages.

# E3PA Requirements
E3PA requires any machine running Java, with a minimum of 2GB of memory. Gradle is also required to build E3PA and pull down dependencies. Docker is required to run the Docker image if you chose that option.
