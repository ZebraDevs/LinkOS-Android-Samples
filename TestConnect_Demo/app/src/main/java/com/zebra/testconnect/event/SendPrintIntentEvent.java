/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect.event;

import java.util.Map;

public class SendPrintIntentEvent {

    private final String templateName;
    private final Map<String, String> variableDataMap;

    public SendPrintIntentEvent(String templateName, Map<String, String> variableDataMap) {
        this.templateName = templateName;
        this.variableDataMap = variableDataMap;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, String> getVariableDataMap() {
        return variableDataMap;
    }
}
