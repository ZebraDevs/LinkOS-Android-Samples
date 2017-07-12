/***********************************************
 CONFIDENTIAL AND PROPRIETARY
 The source code and other information contained herein is the confidential and the exclusive property of
 ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 Copyright ZIH Corp. 2015
 ALL RIGHTS RESERVED
 ***********************************************/

package com.zebra.testconnect;


import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

public class PrintIntentDemoItemDetails {

    private int resultCode = 0;
    private String errorMessage = "";
    private String sentMessage = "";

    private static SparseArray<String> intentErrorCodeMap = null;

    public PrintIntentDemoItemDetails() {
    }

    private void initializeErrorCodeArray (Context context) {
        if(null == intentErrorCodeMap) {
            final Resources resources = context.getResources();
            intentErrorCodeMap = new SparseArray<String>() {{
                put(0, resources.getString(R.string.intent_result_success));
                put(1, resources.getString(R.string.intent_result_no_printer_selected));
                put(2, resources.getString(R.string.intent_result_connection_error));
                put(3, resources.getString(R.string.intent_result_template_read_error));
                put(4, resources.getString(R.string.intent_result_unrecoverable_error));
                put(5, resources.getString(R.string.intent_result_graphic_read_error));
                put(6, resources.getString(R.string.intent_result_illegal_argument));
                put(7, resources.getString(R.string.intent_result_cloud_file_error));
            }};
        }
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSentMessage() {
        return sentMessage;
    }

    public void setSentMessage(String sentMessage) {
        this.sentMessage = sentMessage;
    }

    public String getResultCodeDescription(Context context) {
        initializeErrorCodeArray(context);
        return intentErrorCodeMap.get(resultCode);
    }
}
