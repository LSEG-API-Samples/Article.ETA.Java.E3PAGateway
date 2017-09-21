package com.thomsonreuters.atr.gateway.fix.log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * this class represent JSON payload for fix message
 */
class JsonFixPayload {
    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, String>>(){}.getType();

    private final int version = 1;
    private String hostname;
    private String serverID;
    private String systemType;
    private String connCompID;
    private String guid;
    private String refGuid;
    private Long timestamp;
    private Long timestampToBackend;
    private String msgDir;
    private String msg;
    private Map<String, String> payload = new HashMap<>(30);

    public void setQuoteID(String quoteID) {
        set("QuoteID",quoteID);
    }

    public void setMsgType(String msgType) {
        set("MsgType",msgType);
    }

    public void setSecurityID(String securityID) {
        set("SecurityID",securityID);
    }

    public void setSenderCompID(String senderCompID) {
        set("SenderCompID",senderCompID);
    }

    public void setTargetCompID(String targetCompID) {
        set("TargetCompID",targetCompID);
    }

    public JsonFixPayload() {
        payload.put("version", String.valueOf(version));
    }

    private JsonFixPayload(Map<String, String> payload) {
        if (payload == null){
            throw new NullPointerException("Null pointer: payload");
        }

        this.payload = payload;
        String value = payload.get("hostname");
        if (value != null){
            hostname = value;
        }
        value = payload.get("serverid");
        if (value != null){
            serverID = value;
        }
        value = payload.get("systemtype");
        if (value != null){
            systemType = value;
        }
        value = payload.get("conncompid");
        if (value != null){
            connCompID = value;
        }
        value = payload.get("guid");
        if (value != null){
            guid = value;
        }
        value = payload.get("refguid");
        if (value != null){
            refGuid = value;
        }
        value = payload.get("timestamp");
        if (value != null){
            timestamp = Long.valueOf(value);
        }
        value = payload.get("timestamptobackend");
        if (value != null){
            timestampToBackend = Long.valueOf(value);
        }
        value = payload.get("msgdir");
        if (value != null){
            msgDir = value;
        }
        value = payload.get("msg");
        if (value != null){
            msg = value;
        }
    }

    public void set(String name, Object value){
        if (value == null){
            payload.remove(name);
        } else {
            payload.put(name,value.toString());
        }
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
        set("serverid",serverID);
    }

    public void setConnCompID(String connCompID) {
        this.connCompID = connCompID;
        set("conncompid",connCompID);
    }

    public void setGuid(String guid) {
        this.guid = guid;
        set("guid",guid);
    }

    public void setMsg(String msg) {
        this.msg = msg;
        set("msg",msg);
    }

    public void setMsgDir(FixMessageDirection msgDir) {
        this.msgDir = msgDir.toString();
        set("msgdir",msgDir);
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
        set("hostname",hostname);
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        set("timestamp",timestamp);
    }

    public String toString(){
        return gson.toJson(payload);
    }

    static public JsonFixPayload fromJson(String json){
        Map<String,String> map = gson.fromJson(json, type);
        JsonFixPayload fixMessageTrackerPayload = new JsonFixPayload(map);
        return fixMessageTrackerPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonFixPayload that = (JsonFixPayload) o;

        if (version != that.version) return false;
        if (hostname != null ? !hostname.equals(that.hostname) : that.hostname != null) return false;
        if (serverID != null ? !serverID.equals(that.serverID) : that.serverID != null) return false;
        if (systemType != null ? !systemType.equals(that.systemType) : that.systemType != null) return false;
        if (connCompID != null ? !connCompID.equals(that.connCompID) : that.connCompID != null) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (refGuid != null ? !refGuid.equals(that.refGuid) : that.refGuid != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (timestampToBackend != null ? !timestampToBackend.equals(that.timestampToBackend) : that.timestampToBackend != null)
            return false;
        if (msgDir != null ? !msgDir.equals(that.msgDir) : that.msgDir != null) return false;
        if (msg != null ? !msg.equals(that.msg) : that.msg != null) return false;
        return !(payload != null ? !payload.equals(that.payload) : that.payload != null);

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (serverID != null ? serverID.hashCode() : 0);
        result = 31 * result + (systemType != null ? systemType.hashCode() : 0);
        result = 31 * result + (connCompID != null ? connCompID.hashCode() : 0);
        result = 31 * result + (guid != null ? guid.hashCode() : 0);
        result = 31 * result + (refGuid != null ? refGuid.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (timestampToBackend != null ? timestampToBackend.hashCode() : 0);
        result = 31 * result + (msgDir != null ? msgDir.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }
}
