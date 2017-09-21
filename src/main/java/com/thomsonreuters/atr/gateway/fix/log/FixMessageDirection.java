package com.thomsonreuters.atr.gateway.fix.log;

import java.util.HashMap;
import java.util.Map;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 */
public enum FixMessageDirection {
    Inbound("I"), Outbound("O");
    private String direction;

    FixMessageDirection(String direction) {
        this.direction = direction;
    }

    public String toString(){
        return direction;
    }

    final static Map<String,FixMessageDirection> map;
    static {
        map = new HashMap<>(2);
        for (FixMessageDirection item : values())
            map.put(item.toString(),item);
    }
}
