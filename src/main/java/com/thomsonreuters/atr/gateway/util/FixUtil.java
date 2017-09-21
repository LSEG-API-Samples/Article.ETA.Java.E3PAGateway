package com.thomsonreuters.atr.gateway.util;

import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.MessageUtils;

import java.text.DecimalFormat;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class FixUtil {
    public final static char delimiter = '\u0001';

    public static Group getRepeatingGroup(final FieldMap message, final int tag, final int whichGrp) {
        try {
            if (message.isSetField(tag)) {
                return message.getGroup(whichGrp, tag);
            }
        }catch (final FieldNotFound fnf){
            return null;
        }
        return null;
    }

    public static String processChecksum4FixString(String fix)
    {
        //have tried to remove field from Message, it does not work. This is the only way to make checksum work.
        fix = fix.replaceAll("\u000110=[0-9]*", "");
        int checksum = MessageUtils.checksum(fix);
        final DecimalFormat checksumFormat = new DecimalFormat("000");
        fix += "10=" + checksumFormat.format(checksum) + delimiter;
        return fix;
    }

}
