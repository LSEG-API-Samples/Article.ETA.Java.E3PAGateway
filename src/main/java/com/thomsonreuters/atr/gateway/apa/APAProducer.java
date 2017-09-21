package com.thomsonreuters.atr.gateway.apa;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.thomsonreuters.atr.gateway.fix.FixSessionClient;
import com.thomsonreuters.atr.gateway.fix.FixSessionException;
import com.thomsonreuters.atr.gateway.fix.FixSessionManager;
import com.thomsonreuters.atr.gateway.util.EODUtil;
import com.thomsonreuters.upa.fdm.config.FixDomainJSONConfiguration;
import com.thomsonreuters.upa.fdm.decoders.PostMessageDecoder;
import com.thomsonreuters.upa.fdm.dictionary.DataDictionaryManager;
import com.thomsonreuters.upa.fdm.dictionary.FileBasedDataDictionaryManager;
import com.thomsonreuters.upa.fdm.exceptions.FixDomainException;
import com.thomsonreuters.upa.fdm.messages.FixMessage;
import com.thomsonreuters.upa.fdm.transform.JSONRuleProcessor;
import com.thomsonreuters.upa.framework.client.Producer;
import com.thomsonreuters.upa.framework.dispatcher.ConfigurationStrings;
import com.thomsonreuters.upa.framework.events.PostInfo;
import com.thomsonreuters.upa.framework.events.ReliableProducerStreamInfo;
import com.thomsonreuters.upa.framework.events.StreamInfo;
import com.thomsonreuters.upa.framework.exception.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Class that holds TREP producer and processes TREP messages and status events
 */
@SuppressWarnings("WeakerAccess")
public class APAProducer implements FixSessionClient {

    // We will use simple logger to log all FIX traffic,
    // as well as application messages
    private final static Logger logger = LoggerFactory.getLogger(APAProducer.class);

    // Connection to TREP infrastructure
    private final Producer producer;

    // Private and public streams are collated here so we can correctly
    // route responses from the APA
    private final ConsumerWatchList consumerWatchList = new ConsumerWatchList();

    // Build configuration from command line
    private static CommandLineProcessor commandLineProcessor;

    // Manages FIX connection, sends and receives FIX messages
    private FixSessionManager fixSessionManager;

    private boolean producerReady = false;

    public static final String RulesFileLocation = "UPAFramework.Application.Provider.RulesFile";
    public static final String EodTime = "UPAFramework.FixSessionManager.Schedule.EODTime";
    //public static final String path2Files = "./clienthome/rules/tmp";
    public static final String path2Files = "/etc/APAGateway/rules";

    /**
     * APAProducer constructor - creates APA gateway
     * @throws FixDomainException - thrown for issues with configuration file
     * @throws FrameworkException - thrown for issues initializing TREP
     */
    APAProducer() throws FixDomainException, FrameworkException, FixSessionException {

        // Load config from file
        FixDomainJSONConfiguration configuration = commandLineProcessor.getFixDomainConfig();
        logger.info("My configuration parameters: \n{}\n", configuration.toString());

        // Build our custom post decoder
        PostMessageDecoder postMessageDecoder = buildPostDecoder(configuration);

        if (postMessageDecoder == null) {
            throw new FrameworkException("Unable to create post decoder. Exiting.");
        }

        // Create the producer
        producer = new Producer(configuration, null,null);
        producer.setPostMessageDecoder(postMessageDecoder);


        if (!commandLineProcessor.isNofix()) {
            // This may need some beefing up
            fixSessionManager = new FixSessionManager(configuration, this);
        }
        // Oh yeah. This.
        producer.addSubscriber(this);
    }


    private PostMessageDecoder buildPostDecoder(FixDomainJSONConfiguration configuration) {

        String postFields = configuration.getConfigurationString(ConfigurationStrings.ProviderPostFieldsFile)
                .orElse(FileBasedDataDictionaryManager.DEFAULT_POST_FIELD_DICTIONARY_FILE_NAME);
        String postEnums = configuration.getConfigurationString(ConfigurationStrings.ProviderPostEnumssFile)
                .orElse(FileBasedDataDictionaryManager.DEFAULT_POST_ENUM_TABLE_FILE_NAME);
        Integer timestamp = configuration.getConfigurationInteger(ConfigurationStrings.ProviderPostTimestamp, -1);

        String rulesFile = configuration.getConfigurationString(RulesFileLocation, "");

        String eodTime = configuration.getConfigurationString(EodTime, "");

        try {

            DataDictionaryManager dictionaryManager = new FileBasedDataDictionaryManager(postFields, postEnums);

            if (!rulesFile.isEmpty()) {
                logger.info("Loading {} for rules", rulesFile);

                try {

                    CharSource text = Files.asCharSource(new File(rulesFile), Charsets.UTF_8);

                    if (!text.isEmpty()) {

                        String rules = text.read();

                        logger.info("Rules: {}", rules);
//clean up serial number file
                        EODUtil.cleanSerialNumberFiles(path2Files, eodTime);

                        JSONRuleProcessor ruleProcessor = new JSONRuleProcessor(rules);

                        // todo: make timestamp field configurable
                        return new PostMessageDecoder(dictionaryManager, timestamp, ruleProcessor);

                    } else {
                        logger.error("Error reading rules file at {}", rulesFile);
                        return null;
                    }

                } catch (IOException ioe) {
                    logger.error("Error reading rules file at {}", rulesFile);
                    return null;
                }


            } else {
                // todo: make timestamp field configurable
                return new PostMessageDecoder(dictionaryManager, timestamp);

            }




        } catch (FixDomainException fde) {
            logger.error("Error creating dictionary for posts!");
            return null;
        }
    }


    /**
     * Start producer - called from main() to start TREP message processing
     */
    private void startProducer() {
        try {
            Thread producerThread = producer.start();
            logger.info("Started thread.");
            if (commandLineProcessor.isNofix()) {
                producer.ready();
            } else {
                fixSessionManager.start();
            }
            producerThread.join();
        } catch (InterruptedException ie) {
            logger.error("Producer thread interrupted.");
        } catch (FixSessionException fse) {
            logger.error("Error starting fix session.", fse);
        }
    }

    /**
     * main() - application entry point
     * @param args - first argument should be path to configuration file
     * @throws Exception - exception thrown on startup failure
     */
    public static void main(String[] args) throws Exception {

        commandLineProcessor = new CommandLineProcessor(CommandLineProcessor.CommandlineMode.PROVIDERMODE, args);

        if (args.length > 0) {
            APAProducer producer = new APAProducer();
            logger.info("Starting producer");
            producer.startProducer();
        } else {
            throw new FrameworkException("Incorrect configuration specified on command line.");
        }
    }

    /**
     * Process stream status events from TREP
     * @param streamInfo - stream information passed from TREP
     */
    @Subscribe
    public void processReliableProducerStreamInfo(ReliableProducerStreamInfo streamInfo) {

        String name = streamInfo.getUserName();

        switch (streamInfo.getState()) {
            // We're not supposed to get OPEN statuses.
            case OPEN:
                logger.debug("Unexpected open status: " + name);
                break;

            // We have a request to open a stream. Check name and process.
            case PENDING:
                logger.trace("Received PENDING stream for {}. Accepting", name);
                streamInfo.setState(StreamInfo.StreamState.OPEN);
                consumerWatchList.addNode(name, streamInfo);
                producer.processStream(streamInfo);
                break;
            case CLOSED:
                logger.debug("Close for stream: " + name);
                consumerWatchList.deleteNode(name);
        }
    }

    /**
     * Process a fix message received on a private stream
     * @param fixMessage the message
     */
    @Subscribe
    public void processFrameworkFixMessage(FixMessage fixMessage) {
        try {
            logger.info("Received fix message {} {}", fixMessage.getStreamProperties().getUserName(), fixMessage);

            if (fixSessionManager != null) {
                fixSessionManager.sendFixMessage(fixMessage);
            }
        } catch (FixSessionException fse) {
            logger.error("Error sending fix message {}", fixMessage, fse);
        }
    }

    /**
     * Process an on- or off-stream post message
     * @param postInfo the post message and properties
     */
    @Subscribe
    public void processPostMessage(PostInfo postInfo) {
        try {
            logger.info("Received post message {} {}", postInfo.getPostType(), postInfo.getPostMessage());

            if (fixSessionManager != null) {
                fixSessionManager.sendFixMessage(postInfo.getPostMessage());
            }

        } catch (FixSessionException fse) {
            logger.error("Error sending fix message {}", postInfo.getPostMessage(), fse);
        }
    }

    /**
     * Process fix emssages received from FixSessionManager
     * @param fixMessage the inbound FIX message
     */
    public void processFixMessage(FixMessage fixMessage) {
        logger.info("Received fix message {} {}", fixMessage.getStreamProperties().getUserName(), fixMessage);
    }


    /**
     * Advertise TREP service as up or down based on FIX session state
     * @param sessionState FIX session state
     */
    public void processFixSessionState(FixSessionState sessionState) {

        switch(sessionState) {
            case Up:
                if (!producerReady) {
                    producer.ready();
                    logger.info("Marked producer ready.");
                    producerReady = true;
                }
                break;
            case Down:
            default:
                producer.notReady();
                producerReady = false;
        }
    }

}
