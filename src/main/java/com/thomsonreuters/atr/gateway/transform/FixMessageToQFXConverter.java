package com.thomsonreuters.atr.gateway.transform;

import com.thomsonreuters.upa.fdm.messages.FixMessage;
import com.thomsonreuters.upa.framework.exception.FrameworkException;
import quickfix.Message;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public interface FixMessageToQFXConverter {
    Message convert(FixMessage message, String targetBeginString) throws FrameworkException;
    //FixMessage convert(Message message, String targetBeginString) throws FrameworkException;
}
