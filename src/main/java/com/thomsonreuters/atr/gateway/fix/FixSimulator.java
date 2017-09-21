package com.thomsonreuters.atr.gateway.fix;

import com.thomsonreuters.atr.gateway.apa.CommandLineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;

import java.io.ByteArrayInputStream;
import java.util.Map;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * This is to simulate a FIX engine to receive fix connections
 */
public class FixSimulator {
    private final static Logger logger = LoggerFactory.getLogger(FixSimulator.class);

    public static void main(final String[] args){
        FixSimulator responder = new FixSimulator();
        responder.run(args);
    }

    private void run(final String[] args){
        Map<String, Object> config = null;
        try {
            config = new CommandLineProcessor(CommandLineProcessor.CommandlineMode.SIMULATOR, args).getSimulatorConfig();
        }catch (final Exception e){
            logger.error("Exception happened during command line processing.", e);
            System.exit(1);
        }
        String fileLogPath = "./logs";
        String fileStorePath = "./logs";
        String settings = String.format("[default]\n" +
                        "SocketAcceptPort=%s\n" +
                        "FileLogPath=%s\n" +
                        "FileStorePath=%s\n" +
                        "ConnectionType=acceptor\n" +
                        "ValidateUserDefinedFields=N\n" +
                        "ValidateLengthAndChecksum=N\n" +
                        "ValidateFieldsOutOfOrder=N\n" +
                        "ValidateFieldsHaveValues=N\n" +
                        "ValidateIncomingMessage=N\n" +
                        "SocketUseSSL=%s\n" +
                       // "SocketKeyStore=./logs/x509cert.jks\n" +
                        "SocketKeyStore=%s\n" +
                        "SocketKeyStorePassword=%s\n" +
                        //"UseDataDictionary=N\n" +
                        "SenderCompID=*\n" +
                        "TargetCompID=*\n" +
                        "\n" +
                        "[session]\n" +
                        "BeginString=%s\n" +
                        "SenderCompID=%s\n" +
                        "TargetCompID=%s\n" +
                        "StartTime=00:00:00\n" +
                        "EndTime=23:59:59\n" +
                        "NonStopSession=Y\n", // + "AcceptorTemplate=Y",
                config.get("port"), fileLogPath, fileStorePath, config.get("UseSSL"), config.get("KeyStorePath"), config.get("KeyStorePassword"),  config.get("FixVersion"), config.get("SenderCompID"), config.get("TargetCompID"));
        SessionSettings sessionSettings = null;
        try {
            sessionSettings = new SessionSettings(new ByteArrayInputStream(settings.getBytes("UTF-8")));
            SessionID sessionID = new SessionID((String)config.get("FixVersion"), (String)config.get("SenderCompID"), (String)config.get("TargetCompID"));
            if("FIXT.1.1".equals(config.get("FixVersion"))){
                int defaultApplVerId = 9;
                sessionSettings.setLong(sessionID, "DefaultApplVerID", defaultApplVerId);
            }

        }catch (final Exception e){
            logger.error("Exception happened", e);
        }
        MessageStoreFactory storeFactory = new FileStoreFactory(sessionSettings);
        LogFactory logFactory = new FileLogFactory(sessionSettings);
        //LogFactory logFactory = new JSONLogFactory(instanceId, logPath,filePrefix, fileExtension, poolSize);
        MessageFactory messageFactory = new DefaultMessageFactory();
        Application application = new Application(){

            @Override
            public void onCreate(SessionID sessionId) {
                logger.info("onCreate on SessionID: {}", sessionId);
            }

            @Override
            public void onLogon(SessionID sessionId) {
                logger.info("onLogon on SessionID: {}", sessionId);
            }

            @Override
            public void onLogout(SessionID sessionId) {
                logger.info("onLogout on SessionID: {}", sessionId);
            }

            @Override
            public void toAdmin(Message message, SessionID sessionId) {
                logger.info("toAdmin on SessionID: {}, message: {}", sessionId, message);
            }

            @Override
            public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
                logger.info("fromAdmin on SessionID: {}, message: {}", sessionId, message);
            }

            @Override
            public void toApp(Message message, SessionID sessionId) throws DoNotSend {
                logger.info("toApp on SessionID: {}, message: {}", sessionId, message);
            }

            @Override
            public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
                logger.info("Received message: {}", message);
                respondMessage(message, sessionId);
            }

            private void respondMessage(final Message message, final SessionID sessionID){
                try{
                    String msgType = message.getHeader().getString(MsgType.FIELD);
                    switch (msgType){
                        case MsgType.MASS_QUOTE:
                            sendMassQuoteAck(message, sessionID);
                            break;
                        case MsgType.QUOTE:
                            logger.warn("MsgTyp: {} is received, no response to send", msgType);
                            break;
                        case MsgType.QUOTE_CANCEL:
                            logger.warn("MsgTyp: {} is received, no response to send", msgType);
                            break;
                        default:
                            logger.warn("MsgType: {} is not supported.", msgType);
                            break;

                    }
                }catch (final Exception e){
                    logger.error("Exception happened during respondMessage()", e);
                }
            }

            private void sendMassQuoteAck(final Message message, final SessionID sessionID){
                try {
                    String beginString = message.getHeader().getString(BeginString.FIELD);
                    String senderCompID = message.getHeader().getString(TargetCompID.FIELD);
                    String targetCompID = message.getHeader().getString(SenderCompID.FIELD);
                    String quoteId = message.getString(QuoteID.FIELD);
                    Message resp = new DefaultMessageFactory().create(beginString, "b");
                    resp.getHeader().setString(SenderCompID.FIELD, senderCompID);
                    resp.getHeader().setString(TargetCompID.FIELD, targetCompID);
                    resp.setInt(QuoteStatus.FIELD, 0);//accepted
                    resp.setString(QuoteID.FIELD, quoteId);
                    //Session session = Session.lookupSession(sessionID);
                    boolean success = Session.sendToTarget(resp);
                    logger.info("Message: {} is sent to {}, suceess: {}", resp, sessionID, success);
                }catch (final Exception e){
                    logger.error("Exception happened during sendMassQuoteAck()", e);
                }

            }
        };
        try {
            if(sessionSettings == null)
                throw new Exception("SessionSettings is null, can't create Acceptor");
            Acceptor server = new ThreadedSocketAcceptor(application, storeFactory, sessionSettings, logFactory, messageFactory);
            server.start();
        }catch (final Exception e){
            logger.error("Exception happened", e);
            System.exit(1);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException ie) {
                Thread.interrupted();
            }
        }

    }
}

