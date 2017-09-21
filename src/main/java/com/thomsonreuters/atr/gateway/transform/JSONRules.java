package com.thomsonreuters.atr.gateway.transform;

import com.thomsonreuters.upa.fdm.messages.FixTagContainer;
import com.thomsonreuters.upa.fdm.messages.RuleProcessor;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 *  Simple configurable tag-for-tag mapping class
 */
public class JSONRules implements RuleProcessor {


    public JSONRules() {

    }



    @Override
    public boolean processMessage(FixTagContainer container) {

        container.addTag(9000, "HI THERE!");
        return true;
    }
}
