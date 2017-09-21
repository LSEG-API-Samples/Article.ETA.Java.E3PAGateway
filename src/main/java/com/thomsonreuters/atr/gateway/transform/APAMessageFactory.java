package com.thomsonreuters.atr.gateway.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Group;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageUtils;
import quickfix.field.MsgType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static quickfix.FixVersions.BEGINSTRING_FIXT11;
import static quickfix.FixVersions.FIX50SP2;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
class APAMessageFactory implements MessageFactory{
    private static final Logger logger = LoggerFactory.getLogger(APAMessageFactory.class);
    private final Map<String, MessageFactory> messageFactories = new ConcurrentHashMap<>();

    APAMessageFactory() {
        this.addFactory("FIX.4.0");
        this.addFactory("FIX.4.1");
        this.addFactory("FIX.4.2");
        this.addFactory("FIX.4.3");
        this.addFactory("FIX.4.4");
        this.addFactory("FIXT.1.1");
        this.addFactory("FIX.5.0SP2");
    }

    private void addFactory(String beginString) {
        String packageVersion = beginString.replace(".", "").toLowerCase();

        try {
            this.addFactory(beginString, "quickfix." + packageVersion + ".MessageFactory");
        } catch (ClassNotFoundException e) {
            logger.error("Adding factory", e);
        }

    }

    private void addFactory(String beginString, String factoryClassName) throws ClassNotFoundException {
        Class<? extends MessageFactory> factoryClass;

        try {
            factoryClass = Class.forName(factoryClassName).asSubclass(MessageFactory.class);
        } catch (ClassNotFoundException e) {
            factoryClass = Thread.currentThread().getContextClassLoader().loadClass(factoryClassName).asSubclass(MessageFactory.class);
        }

        this.addFactory(beginString, factoryClass);
    }

    private void addFactory(String beginString, Class<? extends MessageFactory> factoryClass) {
        try {
            MessageFactory e = factoryClass.newInstance();
            this.messageFactories.put(beginString, e);
        } catch (Exception e) {
            throw new RuntimeException("Can\'t instantiate " + factoryClass.getName(), e);
        }
    }


    @Override
    public Message create(String beginString, String msgType) {
        MessageFactory messageFactory = getMessageFactory(beginString, msgType);

        if (messageFactory != null) {
            return messageFactory.create(beginString, msgType);
        }

        Message message = new Message();
        message.getHeader().setString(MsgType.FIELD, msgType);

        return message;
    }

    private MessageFactory getMessageFactory(String beginString, String msgType) {
        MessageFactory messageFactory = messageFactories.get(beginString);
        if (beginString.equals(BEGINSTRING_FIXT11)) {
            if (!MessageUtils.isAdminMessage(msgType)) {
                messageFactory = messageFactories.get(FIX50SP2);
            }
        }
        return messageFactory;
    }

    @Override
    public Group create(String beginString, String msgType, int correspondingFieldID) {
        MessageFactory messageFactory = getMessageFactory(beginString, msgType);
        if(messageFactory != null) {
            if(correspondingFieldID == 627){
                return new Group(627, 628, new int[]{628,629,630,0});
            }else if(correspondingFieldID == 887){
                return new Group(887, 888, new int[]{888,889,0});
            }else if(correspondingFieldID == 576 && (!"AS".equals(msgType) || !"AE".equals(msgType))){//In AS and AE, this repeating group is already implemented.
                return new Group(576, 577, new int[]{577,0});
            }else {
                return messageFactory.create(beginString, msgType, correspondingFieldID);
            }
        } else {
            throw new IllegalArgumentException("Unsupported FIX version: " + beginString);
        }

    }
}
