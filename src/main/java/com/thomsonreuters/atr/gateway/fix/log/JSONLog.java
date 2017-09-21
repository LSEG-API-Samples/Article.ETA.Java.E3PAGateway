package com.thomsonreuters.atr.gateway.fix.log;

import ch.qos.logback.classic.PatternLayout;
import com.thomsonreuters.atr.gateway.util.FixUtil;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class JSONLog implements Log {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JSONLog.class);
    private static org.slf4j.Logger jsonLogger = LoggerFactory.getLogger(java.awt.color.ColorSpace.class);

    private SessionID sessionID;
    private final static String serverId = "APA";
    private static String hostname = "Unknown";
    static {
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException ue) {
            logger.error("Exception happened during getting hostname", ue);
        }
    }

    JSONLog(final SessionID sessionID) throws IOException{
        this.sessionID = sessionID;
        try{
            hostname = InetAddress.getLocalHost().getHostName();
        }catch (final UnknownHostException ue){
            logger.error("Exception happened during getting hostname", ue);
        }
        PatternLayout layout = new PatternLayout(); //("%m%n");
        layout.setPattern("%m%n");
        jsonLogger = createLoggerFor();
    }

    private static org.slf4j.Logger createLoggerFor() {
        return LoggerFactory.getLogger(java.awt.color.ColorSpace.class);
    }

    public static void logMessage(final Message message, final SessionID sessionID, final FixMessageDirection direction) {
        String senderCompID="", targetCompID="", securityID="", msgType="", quoteID="";
        try{
            if(message != null){
                senderCompID = message.getHeader().getString(SenderCompID.FIELD);
                targetCompID = message.getHeader().getString(TargetCompID.FIELD);
                msgType = message.getHeader().getString(MsgType.FIELD);
                if(message.isSetField(QuoteID.FIELD)) {
                    quoteID = message.getString(QuoteID.FIELD);
                }
                Group group = FixUtil.getRepeatingGroup(message, NoQuoteSets.FIELD, 1);
                if(group != null) {
                    Group group2 = FixUtil.getRepeatingGroup(group, NoQuoteEntries.FIELD, 1);
                    if(group2 != null) {
                        securityID = group2.getString(SecurityID.FIELD);
                    }else {
                        logger.warn("Failed to find group for tag: {} in {}", NoQuoteEntries.FIELD, message);
                    }
                }else{
                    if(message.isSetField(SecurityID.FIELD)){
                        securityID = message.getString(SecurityID.FIELD);
                    }
                }
            }else{
                logger.error("message is null");
            }
        }catch (final FieldNotFound im){
            logger.error("Exception happened during converting message string to Message object", im);
        }
        if(jsonLogger != null){
            JsonFixPayload payload = new JsonFixPayload();
            payload.setConnCompID(sessionID.getTargetCompID());
            payload.setServerID(serverId);
            payload.setHostname(hostname);
            payload.setGuid(UUID.randomUUID().toString());
            assert message != null;
            payload.setMsg(message.toString());
            payload.setMsgDir(direction);
            payload.setTimestamp(System.currentTimeMillis());
            payload.setSenderCompID(senderCompID);
            payload.setTargetCompID(targetCompID);
            payload.setMsgType(msgType);
            if(!"".equals(quoteID)) {
                payload.setQuoteID(quoteID);
            }
            if(!"".equals(securityID)){
                payload.setSecurityID(securityID);
            }
            jsonLogger.info(payload.toString());
        }else{
            logger.error("JsonLogger is not initialized");
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void onIncoming(String message) {
    }

    @Override
    public void onOutgoing(String message) {
    }

    @Override
    public void onEvent(String s) {

    }

    @Override
    public void onErrorEvent(String s) {

    }
}
