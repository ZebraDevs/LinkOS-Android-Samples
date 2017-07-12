/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintDemoTemplateData {

    private String templateName = "";
    private List<Map<String, String>> variableData = new ArrayList<>();

    public PrintDemoTemplateData(String templateName) {
        this.templateName = templateName;
    }

    public PrintDemoTemplateData(String templateName, final Map<String, String> variableData) {
        this.templateName = templateName;
        this.variableData.add(variableData);
    }

    public String getTemplateName() {
        return templateName;
    }

    public List<Map<String, String>> getVariableData() {
        return variableData;
    }
}
