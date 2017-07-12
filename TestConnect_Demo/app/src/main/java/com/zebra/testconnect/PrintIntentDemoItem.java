/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;


import java.io.Serializable;
import java.util.Map;

public class PrintIntentDemoItem implements Serializable {

    public static final int CHEVRON_STATE_DOWN = R.drawable.icon_chevron_down_blue;
    public static final int CHEVRON_STATE_UP = R.drawable.icon_chevron_up_blue;

    private final String templateName;
    private IntentState state;
    private int chevronState = CHEVRON_STATE_UP;
    private Map<String, String> variableDataMap;

    public enum IntentState {
        Sending, Success, Error
    }

    public PrintIntentDemoItem(IntentState state, String templateName) {
        this.templateName = templateName;
        this.state = state;
    }

    public void setState(IntentState state) {
        this.state = state;
    }

    public IntentState getState() {
        return state;
    }

    public String getTemplateName() {
        return templateName;
    }

    public int getChevronState() {
        return chevronState;
    }

    public void setChevronState(int chevronState) {
        this.chevronState = chevronState;
    }

    public Map<String, String> getVariableDataMap() {
        return variableDataMap;
    }

    public void setVariableDataMap(Map<String, String> variableDataMap) {
        this.variableDataMap = variableDataMap;
    }
}
