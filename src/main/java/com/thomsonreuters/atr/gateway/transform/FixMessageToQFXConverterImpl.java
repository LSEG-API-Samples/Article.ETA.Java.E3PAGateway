package com.thomsonreuters.atr.gateway.transform;

import com.thomsonreuters.upa.fdm.messages.*;
import com.thomsonreuters.upa.framework.exception.FrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MsgType;

import java.util.ArrayList;
import java.util.Collection;

import static com.thomsonreuters.atr.gateway.transform.ConverterUtils.ommToQfTagID;


/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class FixMessageToQFXConverterImpl implements FixMessageToQFXConverter {
    private static DataDictionaryProvider dataDictionaryProvider = new DefaultDataDictionaryProvider(true);
    private static MessageFactory messageFactory = new APAMessageFactory() ;
    private final static Logger logger = LoggerFactory.getLogger(FixMessageToQFXConverterImpl.class);

    @Override
    public Message convert(FixMessage message, String targetBeginString) throws FrameworkException {
        logger.info("Start converting {} to FIX: {}", message, targetBeginString);
        String msgType = message.getMessageType();
        Message result = createMessage(targetBeginString, msgType);

        DataDictionary sessionDictionary = dataDictionaryProvider.getSessionDataDictionary(targetBeginString);

        Collection<FixTag> tags = message.getTags();
        for (FixTag tag : tags) {
            int tagID = ommToQfTagID(tag.getTagID());
            FieldMap container = getTagContainer(result, sessionDictionary, tagID);
            convertTagToQuickfix(container, tag, msgType, targetBeginString);
        }
        return result;
    }

    //@Override
//    public FixMessage convert(Message message, String targetBeginString) throws FrameworkException {
//        return null;
//    }

    private Message createMessage(String beginString, String msgType) {
        Message message = messageFactory.create(beginString, msgType);
        message.getHeader().setString(MsgType.FIELD, msgType);
        return message;
    }

    private void convertGroupToQuickfix(FieldMap container, FixRepeatingGroupTag group, String msgType, String targetBeginString) {
        int groupTagID = ommToQfTagID(group.getTagID());
        ArrayList<ArrayList<FixTag>> theGroups = group.getTheGroups();
        for (ArrayList<FixTag> theGroup : theGroups) {
            Group qfGroup = createQuickfixGroup(targetBeginString, msgType, groupTagID);
            for (FixTag fixTag : theGroup) {
                convertTagToQuickfix(qfGroup, fixTag, msgType, targetBeginString);
            }
            if (!qfGroup.isEmpty()) {
                container.addGroup(qfGroup);
            }
        }
    }

    private void convertTagToQuickfix(FieldMap container, FixTag tag, String msgType, String targetBeginString) {
        if (tag instanceof StandardTag) {
            setQuickfixFieldValue(container, tag);
        } else if (tag instanceof FixRepeatingGroupTag) {
            convertGroupToQuickfix(container, (FixRepeatingGroupTag) tag, msgType, targetBeginString);
        }else if(tag instanceof EnumTag){
            setQuickfixFieldValue(container, tag);
        }else if(tag instanceof TimestampTag){
            setQuickfixFieldValue(container, tag);
        }
    }

    private void setQuickfixFieldValue(FieldMap container, FixTag fixTag) {
        int tagID = ommToQfTagID(fixTag.getTagID());
        String tagValue = fixTag.getTagValue();
        if(container != null) {
            container.setString(tagID, tagValue);
        }else {
            logger.error("container for {} is null, {}.", fixTag, tagValue);
        }
    }

    private Group createQuickfixGroup(String targetBeginString, String msgType, int tagID) {
        return messageFactory.create(targetBeginString, msgType, ommToQfTagID(tagID));
    }

    private FieldMap getTagContainer(Message result, DataDictionary dictionary, int tagID) {
        FieldMap container;
        if (dictionary.isHeaderField(tagID)) {
            container = result.getHeader();
        } else if (dictionary.isTrailerField(tagID)) {
            container = result.getTrailer();
        } else {
            container = result;
        }
        return container;
    }
}
