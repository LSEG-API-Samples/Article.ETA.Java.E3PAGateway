package com.thomsonreuters.atr.gateway.fix;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Standard exception for FIX Session Manager
 */
public class FixSessionException extends Exception {


    /** Build an exception with a default error message */
    public FixSessionException() {
        super("A FIX Session error occurred.");
    }

    /** Build an exception with supplied error message
     *
     * @param message an error message
     *
     * */
    public FixSessionException(String message) {
        super(message);
    }


}
