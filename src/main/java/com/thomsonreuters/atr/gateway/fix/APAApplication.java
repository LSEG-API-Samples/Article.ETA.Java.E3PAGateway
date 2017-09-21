package com.thomsonreuters.atr.gateway.fix;

import com.thomsonreuters.atr.gateway.fix.log.FixMessageDirection;
import com.thomsonreuters.atr.gateway.fix.log.JSONLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.IOException;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class APAApplication implements Application{
    private static final Logger logger = LoggerFactory.getLogger(APAApplication.class);

    @Override
    public void onCreate(SessionID sessionId) {
        logger.info("onCreate");
    }

    @Override
    public void onLogon(SessionID sessionId) {
        logger.info("onLogon");
    }

    @Override
    public void onLogout(SessionID sessionId) {
        logger.info("onLogout");
        Session sess = Session.lookupSession(sessionId);
        logger.warn("found session: {} for SessionID: {}", sess, sessionId);
        if (sess != null) {
            sess.logout();
            logger.warn("logout session for {}, Session exists in QFX? {}", sessionId, Session.doesSessionExist(sessionId));
            try {
                sess.disconnect("logout", true);
                logger.warn("disconnect session for {}, Session exists in QFX? {}", sessionId, Session.doesSessionExist(sessionId));
                sess.setResponder(null);
                //sess = null;
                logger.warn("set session to null, Session exists in QFX? {}", Session.doesSessionExist(sessionId));
            } catch (IOException ie) {
                logger.error("Exception happened during disconnecting {}", sessionId, ie);
            }

        }
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        logger.info("toAdmin: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        logger.info("fromAdmin: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {
        logger.info("toApp: {}", message);
        JSONLog.logMessage(message, sessionId, FixMessageDirection.Outbound);
    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        logger.info("fromApp: {}", message);
        JSONLog.logMessage(message, sessionId, FixMessageDirection.Inbound);
    }
}
