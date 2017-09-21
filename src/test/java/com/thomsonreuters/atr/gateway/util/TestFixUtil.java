package com.thomsonreuters.atr.gateway.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import quickfix.*;
import quickfix.field.NoQuoteEntries;
import quickfix.field.NoQuoteSets;
import quickfix.field.QuoteEntryID;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class TestFixUtil {
    private quickfix.MessageFactory factory;
    private DataDictionary quickfixDictionary;

    @Before
    public void setUp(){
        factory = new DefaultMessageFactory();
        String beginStr = "FIXT.1.1";
        try {
            if("FIXT.1.1".equals(beginStr)) {
                quickfixDictionary = new DataDictionary("FIX50SP2.xml");
            }else{
                quickfixDictionary = new DataDictionary(beginStr);
            }
        }catch (Exception e){
            Assert.fail("Exception happened during creating fix dictionary");
        }
    }

    @Test
    public void testGetRepeatingGroup(){
        String fixString = "8=FIXT.1.1\u00019=144\u000135=i\u000134=11\u000149=TTI52\u000152=20170608-17:47:27.963\u000156=TTB52\u0001117=APAGW-1000\u0001296=1\u0001302=1001\u0001304=1\u0001295=1\u0001299=1\u000148=8675309\u000122=1\u0001132=\u0001134=5000\u000160=00000-00:00:00\u000110=053\u0001";
        //fixString = FixUtil.processChecksum4FixString(fixString);
        try {
            Message qfxMessage = MessageUtils.parse(factory, quickfixDictionary, fixString);
            Assert.assertTrue(true);
            Group group = FixUtil.getRepeatingGroup(qfxMessage, NoQuoteSets.FIELD, 1);
            Assert.assertNotNull(group);
            Group group2 = FixUtil.getRepeatingGroup(group, NoQuoteEntries.FIELD, 1);
            Assert.assertNotNull(group2);
            Assert.assertEquals("1", group2.getString(QuoteEntryID.FIELD));
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }
}
