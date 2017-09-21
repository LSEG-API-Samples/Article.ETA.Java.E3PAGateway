package com.thomsonreuters.atr.gateway.fix.log;

import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public class FixField {
    private static final Logger LOG = LoggerFactory.getLogger(FixField.class);
    private static Set<FixField> set = new HashSet<>(30);

    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<Integer, String>>(){}.getType();

    private int tag;
    private String atrName;

    @Override
    public String toString() {
        return "FixField{" +
                "tag=" + tag +
                ", atrName='" + atrName + '\'' +
                '}';
    }

    public FixField(int tag, String atrName) {
        this(tag, atrName, false);
    }

    public FixField(int tag, String atrName, boolean noCache) {
        this.tag = tag;
        this.atrName = atrName.toLowerCase();
        if (!noCache){
            set.add(this);
        }
    }

    public static FixField createInstance(int tag, String atrName){
        return new FixField(tag,atrName,true);
    }


    public int getTag() {
        return tag;
    }

    public String getAtrName() {
        return atrName;
    }

    /**
     * Sample json string: "{ \"9\":\"BeginString\", \"34\":\"MsgSeqNum\" }"
     * @param json  String
     * @return List of FixField
     */
    public static List<FixField> fromJson(String json){
        Map<Integer,String> map = gson.fromJson(json, type);
        List<FixField> list = new ArrayList<>(map.size());
        map.forEach((k,v) -> {
            list.add(FixField.createInstance(k,v));
        });
        return list;
    }

    /**
     * Sample key value pair string: "2=advid,3=advrefid,4=side,5=transtype,6=avgpx,11=clordid,14=cumqty,20=exectranstype,22=securityidsource,23=ioiid,26=ioirefid,27=size,28=transtype,31=lastpx,32=lastshares,34=msgseq,35=msgtype,37=orderid,38=ordqty,39=orderstatus,41=origclordid,44=price,48=securityid,53=size,54=side,55=symbol,58=text,115=onbehalfofcompid,128=delivertocompid,150=exectype,151=leavesqty"
     * @param keyValues String
     * @return List of FixField
     */
    public static List<FixField> fromKeyValues(String keyValues){
        LOG.info("FIX keyValues: " + keyValues);
        String tokens[] = keyValues.split(" *, *");
        if (tokens == null || tokens.length == 0){
            return new ArrayList<FixField>();
        }
        List<FixField> list = new ArrayList<>(tokens.length);
        for(String keyValue : tokens) {
            if (keyValue.trim().isEmpty()){
                continue;
            }
            String[] pairs = keyValue.split(" *= *", 2);
            list.add(FixField.createInstance(Integer.valueOf(pairs[0]), pairs[1]));
        }
        return list;
    }
}
