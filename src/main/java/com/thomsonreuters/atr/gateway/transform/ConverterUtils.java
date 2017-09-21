package com.thomsonreuters.atr.gateway.transform;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
class ConverterUtils {
    static int ommToQfTagID(int tagID) {
        return tagID & 0xffff;
    }
}
