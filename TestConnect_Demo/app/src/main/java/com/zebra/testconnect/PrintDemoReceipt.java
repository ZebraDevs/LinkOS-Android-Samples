/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PrintDemoReceipt extends PrintDemo {

    private static final String LOG_TAG = PrintDemoReceipt.class.getName();

    private static final int RECEIPT_HEADER_SIZE = 519;
    private static final int RECEIPT_LINE_SIZE = 234;
    private static final int RECEIPT_FOOTER_SIZE = 499;

    public PrintDemoReceipt(Context context) {
        super(context, new ArrayList<PrintDemoItem>());

        int sampleDataResourceId = R.raw.receipt_json;
        byte[] sampleJsonData = readRawResourceFile(sampleDataResourceId);
        sampleDataList = readPrintDemoSampleData(sampleJsonData);
    }

    public String getTitle() {
        return context.getString(R.string.receipt_demo_header);
    }

    public ArrayList<String> getTemplateFileNames() {
        ArrayList<String> templateNames = new ArrayList<>();
        templateNames.add(context.getString(R.string.receipt_header_template_name));
        templateNames.add(context.getString(R.string.receipt_line_template_name));
        templateNames.add(context.getString(R.string.receipt_footer_template_name));
        return templateNames;
    }

    private static String getDateTime() {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date gregorianTime = gregorianCalendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy @ h:mma", Locale.US);
        return dateFormat.format(gregorianTime);
    }

    private double getTotalPrice() {
        double totalPrice = 0;
        for (PrintDemoItem item : getPrintDemoItems()) {
            try {
                totalPrice += Double.parseDouble(item.getPrice().replace("$", ""));
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);
            }
        }
        return totalPrice;
    }

    public Map<String, String> buildHeaderVarsMap() {
        Map<String, String> varMap = new HashMap<>();

        varMap.put("var_company",context.getString(R.string.sample_receipt));
        varMap.put("var_address1", context.getString(R.string.sample_street_name));
        varMap.put("var_address2", context.getString(R.string.sample_city_state));
        varMap.put("var_phone", context.getString(R.string.sample_phone_number));
        varMap.put("var_store", context.getString(R.string.sample_store_number));
        varMap.put("var_datetime", getDateTime());
        return varMap;
    }

    @Override
    public String getHumanReadableTemplateSize() {
        int lineItems = getPrintDemoItems().size();
        int totalSize = RECEIPT_HEADER_SIZE + (lineItems * RECEIPT_LINE_SIZE) + RECEIPT_FOOTER_SIZE;
        return getHumanReadableByteCount(totalSize);
    }

    public Map<String, String> buildLineItemVarsMap(PrintDemoItem demoItem) {
        Map<String, String> varMap = new HashMap<>();
        varMap.put("var_itemdesc", demoItem.getDescription());
        varMap.put("var_price", demoItem.getPrice());
        return varMap;
    }

    public Map<String, String> buildFooterVarsMap() {
        Map<String, String> varMap = new HashMap<>();
        varMap.put("var_total", "$" + String.format("%.2f", getTotalPrice()));
        return varMap;
    }

    public List<PrintDemoTemplateData> buildPrintDemoTemplateData() {
        templateDataList.clear();

        templateDataList.add(new PrintDemoTemplateData(context.getString(R.string.receipt_header_template_name), buildHeaderVarsMap()));

        PrintDemoTemplateData templateData = new PrintDemoTemplateData(context.getString(R.string.receipt_line_template_name));
        List<PrintDemoItem> demoItems = getSampleDataList().get(getCurrentSampleDataIndex()).getItemList();
        for (PrintDemoItem demoItem : demoItems) {
            templateData.getVariableData().add(buildLineItemVarsMap(demoItem));
        }
        templateDataList.add(templateData);

        templateDataList.add(new PrintDemoTemplateData(context.getString(R.string.receipt_footer_template_name), buildFooterVarsMap()));
        return templateDataList;
    }
}
