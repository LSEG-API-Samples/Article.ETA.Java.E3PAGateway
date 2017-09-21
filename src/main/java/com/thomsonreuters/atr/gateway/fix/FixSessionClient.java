package com.thomsonreuters.atr.gateway.fix;


/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Interface for a Fix Session Manager client
 */
public interface FixSessionClient {

    enum FixSessionState {
        Up,
        Down
    }

    void processFixSessionState(FixSessionState sessionState);

}
