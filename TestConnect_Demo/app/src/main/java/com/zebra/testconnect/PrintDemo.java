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

import com.fasterxml.jackson.jr.ob.JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class PrintDemo implements Serializable {
    private static final String LOG_TAG = PrintDemo.class.getName();

    protected transient Context context;
    protected List<PrintDemoItem> printDemoItems;
    protected static List<PrintDemoSampleData> sampleDataList = new ArrayList<>();
    protected static List<PrintDemoTemplateData> templateDataList = new ArrayList<>();

    private static int currentSampleDataIndex;

    public PrintDemo(Context context, List<PrintDemoItem> printDemoItems) {
        this.context = context;
        this.printDemoItems = printDemoItems;

        currentSampleDataIndex = 0;
    }

    public String getTitle() {
        return "";
    }

    public ArrayList<String> getTemplateFileNames() {
        return new ArrayList<>();
    }

    public List<PrintDemoSampleData> getSampleDataList() {
        return sampleDataList;
    }

    public List<PrintDemoItem> getPrintDemoItems() {
        return printDemoItems;
    }

    public abstract Map<String, String> buildHeaderVarsMap();

    public abstract String getHumanReadableTemplateSize();

    public int getCurrentSampleDataIndex() {
        return currentSampleDataIndex;
    }

    public void setCurrentSampleDataIndex(int currentSampleDataIndex) {
        PrintDemo.currentSampleDataIndex = currentSampleDataIndex;
    }

    public List<PrintDemoTemplateData> buildPrintDemoTemplateData() {
        return templateDataList;
    }

    public Map<String, String> buildLineItemVarsMap(PrintDemoItem demoItem) {
        return null;
    }

    public Map<String, String> buildFooterVarsMap() {
        return null;
    }

    protected String getHumanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public List<PrintDemoSampleData> readPrintDemoSampleData(byte[] sampleJsonData) {
        List<PrintDemoSampleData> demoSampleData = null;

        try {
            demoSampleData = JSON.std.listOfFrom(PrintDemoSampleData.class, sampleJsonData);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getLocalizedMessage(), e);
        }
        return demoSampleData;
    }

    public byte[] readRawResourceFile(int resourceId) {
        ByteArrayOutputStream resourceByteStream = new ByteArrayOutputStream();

        if (context != null) {
            try {
                InputStream inputStream = context.getResources().openRawResource(resourceId);

                int b = inputStream.read();
                while (b != -1) {
                    resourceByteStream.write(b);
                    b = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error reading resource file", e);
            }
        }
        return resourceByteStream.toByteArray();
    }
}
