package com.thomsonreuters.atr.gateway.fix.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Log;
import quickfix.LogFactory;
import quickfix.SessionID;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class JSONLogFactory implements LogFactory {
    private static final Logger logger = LoggerFactory.getLogger(JSONLogFactory.class);

    public JSONLogFactory() {
    }

    @Deprecated
    @Override
    public Log create() {
        return null;
    }

    @Override
    public Log create(SessionID sessionID) {
        try {
            return new JSONLog(sessionID);
        } catch (Exception e) {
            logger.error("Exception happened during creating Log", e);
        }
        return null;
    }
}
