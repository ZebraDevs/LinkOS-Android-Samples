/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintDemoShelfLabel extends PrintDemo {

    private static final int LABEL_TEMPLATE_SIZE = 312;

    public PrintDemoShelfLabel(Context context) {
        super(context, new ArrayList<PrintDemoItem>());

        int sampleDataResourceId = R.raw.price_tag_json;
        byte[] sampleJsonData = readRawResourceFile(sampleDataResourceId);
        sampleDataList = readPrintDemoSampleData(sampleJsonData);
    }

    public String getTitle() {
        return context.getString(R.string.shelf_label_demo_header);
    }

    public ArrayList<String> getTemplateFileNames() {
        return new ArrayList<>(Collections.singletonList(context.getString(R.string.shelf_label_template_name)));
    }

    public Map<String, String> buildHeaderVarsMap() {
        Map<String, String> varMap = new HashMap<>();

        PrintDemoItem demoItem = printDemoItems.get(0);
        varMap.put("var_item", demoItem.getDescription());
        varMap.put("var_price", demoItem.getPrice());
        varMap.put("var_upc", demoItem.getUpc());
        return varMap;
    }

    @Override
    public String getHumanReadableTemplateSize() {
        return getHumanReadableByteCount(LABEL_TEMPLATE_SIZE);
    }

    public List<PrintDemoTemplateData> buildPrintDemoTemplateData() {
        templateDataList.clear();

        templateDataList.add(new PrintDemoTemplateData(context.getString(R.string.shelf_label_template_name), buildHeaderVarsMap()));
        return templateDataList;
    }
}
