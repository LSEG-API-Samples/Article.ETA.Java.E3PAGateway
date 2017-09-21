package com.thomsonreuters.atr.gateway.fix;

import com.thomsonreuters.atr.gateway.fix.log.JSONLogFactory;
import com.thomsonreuters.atr.gateway.transform.FixMessageToQFXConverter;
import com.thomsonreuters.atr.gateway.transform.FixMessageToQFXConverterImpl;
import com.thomsonreuters.upa.fdm.config.IFixDomainConfiguration;
import com.thomsonreuters.upa.fdm.messages.FixMessage;
import com.thomsonreuters.upa.framework.exception.FrameworkException;
import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;

import javax.management.JMException;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Class that allows an app to send and receive messages from a single fix session
 */
public class FixSessionManager {

    // Logs all FIX traffic
    private final Logger logger = LoggerFactory.getLogger(FixSessionManager.class);

    /**
     * Our one-and-only client, for now
     */
    private FixSessionClient fixSessionClient;

    private String fixVersion;
    private String senderCompID;
    private String targetCompID;

    /**
     * connection to FIX Server
     */
    private Initiator initiator;

    private static ThreadLocal<FixMessageToQFXConverter> converterLocal  = new ThreadLocal<FixMessageToQFXConverter>(){
        @Override protected FixMessageToQFXConverter initialValue() {
            return new FixMessageToQFXConverterImpl();
        }
    };

    /**
     * Class constructor
     * @param configuration configuration file
     * @param fixSessionClient our client
     * @throws FixSessionException thrown on session set up error
     */
    public FixSessionManager(IFixDomainConfiguration configuration, FixSessionClient fixSessionClient) throws FixSessionException{

        if (fixSessionClient == null) {
            throw new FixSessionException("Null client");
        } else {
            this.fixSessionClient = fixSessionClient;
        }
        Optional<Initiator> optInitiator = initializeFIXConnection(configuration);
        if(optInitiator.isPresent()) {
            this.initiator = optInitiator.get();
        }
    }

    private Optional<Initiator> initializeFIXConnection(IFixDomainConfiguration configuration) throws FixSessionException{
        try {
        this.fixVersion = configuration.getConfigurationString(ConfigurationStrings.Version)
                .orElseThrow(() -> new FrameworkException("No FIXVersion specified"));
        this.senderCompID = configuration.getConfigurationString(ConfigurationStrings.SenderCompId)
                .orElseThrow(() -> new FrameworkException("No senderCompID specified"));
        this.targetCompID = configuration.getConfigurationString(ConfigurationStrings.TargetCompId)
                .orElseThrow(() -> new FrameworkException("No targetCompID specified"));
        String ipAddress = configuration.getConfigurationString(ConfigurationStrings.Address)
                .orElseThrow(() -> new FrameworkException("No ipAddress specified"));
        String port = configuration.getConfigurationString(ConfigurationStrings.Port)
                .orElseThrow(() -> new FrameworkException("No port specified"));
        String hbInterval = configuration.getConfigurationString(ConfigurationStrings.Heartbeat)
                .orElseThrow(() -> new FrameworkException("No heartbeatInterval specified"));
        //those two take default value
            String fileLogPath = configuration.getConfigurationString(ConfigurationStrings.FILELogPath)
                    .orElseThrow(() -> new FrameworkException("No FileLogPath specified"));
            String startTime = configuration.getConfigurationString(ConfigurationStrings.STARTTIME)
                    .orElseThrow(() -> new FrameworkException("No StartTime specified"));
            String endTime = configuration.getConfigurationString(ConfigurationStrings.ENDTIME)
                    .orElseThrow(() -> new FrameworkException("No EndTime specified"));
            String useSSL = configuration.getConfigurationString(ConfigurationStrings.USESSL)
                    .orElseThrow(() -> new FrameworkException("No UseSSL specified"));

            String settings = String.format("[default]\n" +
                        "ConnectionType=initiator\n" +
                        "ValidateUserDefinedFields=N\n" +
                        "RequiresOrigSendingTime=N\n"+
                        "FileLogPath=%s\n" +
                        "FileStorePath=%s\n" +
                        "SocketUseSSL=%s\n" +
                        "\n" +
                        "[session]\n" +
                        "BeginString=%s\n" +
                        "TargetCompID=%s\n" +
                        "SenderCompID=%s\n" +
                        "HeartBtInt=%s\n" +
                        "SocketConnectPort=%s\n" +
                        "SocketConnectHost=%s\n" +
                        // We get an error if StartTime and EndTime aren't incluced, even if we
                        // specify NonStopSession.  Presumabley they aren't used.
                        "StartTime=%s\n" +
                        "EndTime=%s\n" +
                        "NonStopSession=Y\n",
                fileLogPath, fileLogPath, useSSL, fixVersion, targetCompID, senderCompID, hbInterval, port, ipAddress, startTime, endTime);
            SessionSettings sessionSettings = new SessionSettings(new ByteArrayInputStream(settings.getBytes("UTF-8")));
            if("FIXT.1.1".equals(fixVersion)){
                SessionID sessionID = new SessionID(fixVersion, senderCompID, targetCompID);
                int defaultApplVerId = 9;
                sessionSettings.setLong(sessionID, "DefaultApplVerID", defaultApplVerId);
            }

            MessageStoreFactory storeFactory = new FileStoreFactory(sessionSettings);
            LogFactory logFactory = new JSONLogFactory();
            MessageFactory messageFactory = new DefaultMessageFactory();
            Application application = new APAApplication();
            //need log factory, otherwise event will log to console
            Initiator initiator = new ThreadedSocketInitiator(application, storeFactory, sessionSettings, logFactory, messageFactory);
            try {
                JmxExporter jmxExporter = new JmxExporter();
                ObjectName connectorObjectName = jmxExporter.register(initiator);
                logger.info("Registered Initiator with JMX: {}", connectorObjectName);
            }catch(final JMException je){
                logger.error("Exception happened during registering initiator with JMX", je);
            }
            return Optional.of(initiator);



        } catch (UnsupportedEncodingException| ConfigError |FrameworkException uee) {
            // really shouldn't happen...
            logger.error("encoding SessionSettings",uee);
            throw new FixSessionException("encoding SessionSettings: " + uee.getMessage());
        }

    }


    public void sendFixMessage(FixMessage fixMessage) throws FixSessionException {
        logger.info("Sending fix message: {}", fixMessage);
        Message qfxMsg;
        try {
            //convert FixMessage to qfx Message
            qfxMsg = converterLocal.get().convert(fixMessage, this.fixVersion);
            if(qfxMsg == null){
                logger.error("Can't convert {} to QuickFix Message object. Don't send FIX message to server", fixMessage);
                return;
            }
            logger.info("FixMessage is converted to QuickFix message: {}", qfxMsg);
            qfxMsg.getHeader().setString(SenderCompID.FIELD, this.senderCompID);
            qfxMsg.getHeader().setString(TargetCompID.FIELD, this.targetCompID);
            SessionID sessionID = new SessionID(qfxMsg.getHeader().getString(BeginString.FIELD), this.senderCompID, this.targetCompID);
            boolean success;

            Session session = Session.lookupSession(sessionID);
            success = session.send(qfxMsg);
            logger.info("Message sent: {}, successful: {}", qfxMsg, success);
        }catch (final FrameworkException| FieldNotFound fe)
        {
            logger.error("Exception happened during sending FIX message to wire", fe);
        }


    }

    public Thread start() throws FixSessionException {

        if(initiator != null){
            try {
                initiator.start();
            }catch (final ConfigError ce){
                logger.error("Exception happened during starting initiator. Exit", ce);
                System.exit(1);
            }
        }else {
            logger.error("Initiator has not been initialized. Exit");
            System.exit(1);
        }
        fixSessionClient.processFixSessionState(FixSessionClient.FixSessionState.Up);

        return new Thread();

    }

//    public void stop() {
//        if(initiator.isPresent()){
//            initiator.get().stop();
//        }
//
//    }


}
