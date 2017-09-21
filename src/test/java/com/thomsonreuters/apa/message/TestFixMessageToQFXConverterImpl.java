package com.thomsonreuters.apa.message;

import com.thomsonreuters.atr.gateway.transform.FixMessageToQFXConverter;
import com.thomsonreuters.atr.gateway.transform.FixMessageToQFXConverterImpl;
import com.thomsonreuters.upa.fdm.dictionary.DataDictionaryManager;
import com.thomsonreuters.upa.fdm.dictionary.FileBasedDataDictionaryManager;
import com.thomsonreuters.upa.fdm.encoders.FixToFixMessage;
import com.thomsonreuters.upa.fdm.messages.FixMessage;
import org.junit.Rule;
import org.junit.Test;
import quickfix.Message;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class TestFixMessageToQFXConverterImpl {

    @Rule
    public MessageRule messageRule = new MessageRule();

    @Test
    @MessageRule.ExpectedTag(tag = 58, message ="test")
    @MessageRule.ExpectedTag(tag = 35, message ="AE")
    public void testConvertAEMessage() throws Exception {
        DataDictionaryManager ommDictionaryManager = new FileBasedDataDictionaryManager("etc/FDMFixFieldDictionary", "etc/FDMenumtypes.def");
        FixToFixMessage fixToFixMessage = new FixToFixMessage(ommDictionaryManager);
        String ae = "8=FIXT.1.1|9=145|35=AE|34=194|49=TTI44_01|52=20170509-13:25:50.974|56=TTB44_01|31=99.09|32=2999|58=test|60=20170509-13:25:50.974|75=20170508|570=N|A571=12345|10=199|";
        FixMessage fixMessage = fixToFixMessage.encode(ae, '|');
        FixMessageToQFXConverter converter = new FixMessageToQFXConverterImpl();
        Message msg = converter.convert(fixMessage, "FIXT.1.1");
        MessageRule.result = msg;
    }

    @Test
    @MessageRule.ExpectedTag(tag = 58, message ="test")
    @MessageRule.ExpectedTag(tag = 56, message ="TTB44_01")
    @MessageRule.ExpectedTag(tag = 49, message ="TTI44_01")
    public void testConvertiMessage() throws Exception {
        DataDictionaryManager ommDictionaryManager = new FileBasedDataDictionaryManager("etc/FDMFixFieldDictionary", "etc/FDMenumtypes.def");
        FixToFixMessage fixToFixMessage = new FixToFixMessage(ommDictionaryManager);
        String ae = "8=FIXT.1.1|9=145|35=i|34=194|49=TTI44_01|52=20170509-13:25:50.974|56=TTB44_01|31=99.09|32=2999|58=test|60=20170509-13:25:50.974|75=20170508|570=N|571=12345|10=199|";
        FixMessage fixMessage = fixToFixMessage.encode(ae, '|');
        FixMessageToQFXConverter converter = new FixMessageToQFXConverterImpl();
        Message msg = converter.convert(fixMessage, "FIXT.1.1");
        MessageRule.result = msg;
    }

    @Test
    @MessageRule.ExpectedTag(tag = 58, message ="test")
    @MessageRule.ExpectedTag(tag = 32, message ="2999")
    public void testConvertSMessage() throws Exception {
        DataDictionaryManager ommDictionaryManager = new FileBasedDataDictionaryManager("etc/FDMFixFieldDictionary", "etc/FDMenumtypes.def");
        FixToFixMessage fixToFixMessage = new FixToFixMessage(ommDictionaryManager);
        String ae = "8=FIXT.1.1|9=145|35=S|34=194|49=TTI44_01|52=20170509-13:25:50.974|56=TTB44_01|31=99.09|32=2999|58=test|60=20170509-13:25:50.974|75=20170508|570=N|571=12345|10=199|";
        FixMessage fixMessage = fixToFixMessage.encode(ae, '|');
        FixMessageToQFXConverter converter = new FixMessageToQFXConverterImpl();
        Message msg = converter.convert(fixMessage, "FIXT.1.1");
        MessageRule.result = msg;
    }

    @Test
    @MessageRule.ExpectedTag(tag = 58, message ="test")
    @MessageRule.ExpectedTag(tag = 31, message ="99.09")
    @MessageRule.ExpectedTag(tag = 571, message ="12345")
    @MessageRule.ExpectedTag(tag = 570, message ="N")
    public void testConvertZMessage() throws Exception {
        DataDictionaryManager ommDictionaryManager = new FileBasedDataDictionaryManager("etc/FDMFixFieldDictionary", "etc/FDMenumtypes.def");
        FixToFixMessage fixToFixMessage = new FixToFixMessage(ommDictionaryManager);
        String ae = "8=FIXT.1.1|9=145|35=Z|34=194|49=TTI44_01|52=20170509-13:25:50.974|56=TTB44_01|31=99.09|32=2999|58=test|60=20170509-13:25:50.974|75=20170508|570=N|571=12345|10=199|";
        FixMessage fixMessage = fixToFixMessage.encode(ae, '|');
        FixMessageToQFXConverter converter = new FixMessageToQFXConverterImpl();
        Message msg = converter.convert(fixMessage, "FIXT.1.1");
        MessageRule.result = msg;
    }
}
